package it.polito.wa2.server.keycloak

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.BadRequestProfileException
import it.polito.wa2.server.BadRequestUserException
import it.polito.wa2.server.ProfileNotFoundException
import it.polito.wa2.server.passwordReset.PasswordResetService
import it.polito.wa2.server.profiles.ProfileRole
import it.polito.wa2.server.profiles.ProfileService
import it.polito.wa2.server.security.WebSecurityConfig
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service @Transactional @Observed
class KeycloakServiceImpl(
    private val keycloakConfig: KeycloakConfig,
    private val profileService: ProfileService,
    private val passwordResetService: PasswordResetService
): KeycloakService {
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
    }

    @PreAuthorize("hasRole('${WebSecurityConfig.MANAGER}')")
    override fun addExpert(userDTO: UserDTO) {
        val user = createUser(userDTO)
        addUser(user)
        keycloakConfig.assignRoles(user.email, listOf(EXPERT))
        profileService.addProfileWithRole(userDTO.toProfileDTO(), ProfileRole.EXPERT)
    }

    override fun resetPassword(email: String) {

        val uuid = UUID.randomUUID()
        passwordResetService.addPasswordReset(email, uuid)
        keycloakConfig.resetPassword(email, uuid)
    }

    override fun applyResetPassword(passwordDTO: PasswordDTO) {
        if(passwordResetService.checkIsValid(passwordDTO.email, passwordDTO.token)) {
            keycloakConfig.applyResetPassword(passwordDTO.email, passwordDTO.password)
            passwordResetService.delete(passwordDTO.token)
        }
        else
            throw BadRequestUserException("Invalid request, ask for a new reset link")
    }

    @PreAuthorize("hasRole('${WebSecurityConfig.MANAGER}')")
    override fun addVendor(userDTO: UserDTO) {
        val user = createUser(userDTO)
        addUser(user)
        keycloakConfig.assignRoles(user.email, listOf(VENDOR))
        profileService.addProfileWithRole(userDTO.toProfileDTO(), ProfileRole.VENDOR)
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

    private fun addUser(user: UserRepresentation){
        keycloakConfig.getRealm().users().create(user)
    }
}