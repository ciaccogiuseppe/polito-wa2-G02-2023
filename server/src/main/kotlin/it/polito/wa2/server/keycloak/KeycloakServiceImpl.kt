package it.polito.wa2.server.keycloak

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.*
import it.polito.wa2.server.emailVerification.EmailVerificationService
import it.polito.wa2.server.passwordReset.PasswordResetService
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileRole
import it.polito.wa2.server.profiles.ProfileService
import it.polito.wa2.server.security.WebSecurityConfig
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
@Observed
class KeycloakServiceImpl(
    private val keycloakConfig: KeycloakConfig,
    private val profileService: ProfileService,
    private val profileRepository: ProfileRepository,
    private val passwordResetService: PasswordResetService,
    private val emailVerificationService: EmailVerificationService
) : KeycloakService {
    companion object {
        const val CLIENT = "app_client"
        const val EXPERT = "app_expert"
        const val VENDOR = "app_vendor"
        //const val MANAGER = "app_manager"
    }

    override fun addClient(userDTO: UserDTO) {
        val user = createUser(userDTO)
        addUser(user)
        keycloakConfig.assignRoles(user.email, listOf(CLIENT))
        profileService.addProfileWithRole(userDTO.toProfileDTO(), ProfileRole.CLIENT)
        validateEmail(user.email)
    }

    @PreAuthorize("hasRole('${WebSecurityConfig.MANAGER}')")
    override fun addExpert(userDTO: UserDTO) {
        val user = createUser(userDTO)
        addUser(user)
        keycloakConfig.assignRoles(user.email, listOf(EXPERT))
        profileService.addProfileWithRole(userDTO.toProfileDTO(), ProfileRole.EXPERT)
        validateEmail(user.email)
    }

    override fun resetPassword(email: String) {

        val uuid = UUID.randomUUID()
        passwordResetService.addPasswordReset(email, uuid)
        keycloakConfig.resetPassword(email, uuid)
    }
    override fun validateEmail(email: String) {
        val isValid = profileService.getIsValid(email)
            ?:  throw ProfileNotFoundException("User with email $email not found")

        if (!isValid){
            val uuid = UUID.randomUUID()
            emailVerificationService.addEmailVerification(email, uuid)
            keycloakConfig.sendValidateMail(email, uuid)
        }
        else {
            throw UnprocessableUserException("Account has been already validated")
        }
    }

    override fun applyValidateEmail( token: UUID) {

        if(emailVerificationService.checkIsValid(token)){
            val email = emailVerificationService.getEmail(token)
                ?: throw ProfileNotFoundException("Token is not associated to any account")
            keycloakConfig.applyValidateUser(email)
            emailVerificationService.delete(token)
            profileService.validateProfile(email)
        }
    }

    override fun applyResetPassword(passwordDTO: PasswordDTO) {
        if (passwordResetService.checkIsValid(passwordDTO.email, passwordDTO.token)) {
            keycloakConfig.applyResetPassword(passwordDTO.email, passwordDTO.password)
            passwordResetService.delete(passwordDTO.token)
        } else
            throw BadRequestUserException("Invalid request, ask for a new reset link")
    }

    @PreAuthorize("hasRole('${WebSecurityConfig.MANAGER}')")
    override fun addVendor(userDTO: UserDTO) {
        val user = createUser(userDTO)
        addUser(user)
        keycloakConfig.assignRoles(user.email, listOf(VENDOR))
        profileService.addProfileWithRole(userDTO.toProfileDTO(), ProfileRole.VENDOR)
        validateEmail(user.email)
    }

    @PreAuthorize("isAuthenticated()")
    override fun updateUser(email: String, userDTO: UserUpdateDTO) {
        if (email != userDTO.email)
            throw BadRequestProfileException("Email in path doesn't match the email in the body")
        // An email is associated with, at most, one user
        val user = keycloakConfig.getRealm().users().searchByEmail(email, true).firstOrNull()
            ?: throw ProfileNotFoundException("Profile with email '${email}' not found")

        user.email = userDTO.email
        user.firstName = userDTO.firstName
        user.lastName = userDTO.lastName

        keycloakConfig.getRealm().users().get(user.id).update(user)
        profileService.updateProfile(email, userDTO.toProfileDTO())
    }


    private fun createUser(userDTO: UserDTO): UserRepresentation {
        val credentials: CredentialRepresentation =
            Credentials.createPasswordCredentials(userDTO.password)
        val user = UserRepresentation()
        user.username = userDTO.username
        user.firstName = userDTO.firstName
        user.lastName = userDTO.lastName
        user.email = userDTO.email
        user.credentials = Collections.singletonList(credentials)
        user.isEnabled = true
        return user
    }

    private fun addUser(user: UserRepresentation) {
        if(profileRepository.findByEmail(user.email) != null)
            throw DuplicateProfileException("Profile with email '${user.email}' already exists")
        keycloakConfig.getRealm().users().create(user)
    }
}