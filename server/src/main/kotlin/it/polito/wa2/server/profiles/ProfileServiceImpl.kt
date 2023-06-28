package it.polito.wa2.server.profiles

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.*
import it.polito.wa2.server.addresses.*
import it.polito.wa2.server.categories.Category
import it.polito.wa2.server.categories.CategoryRepository
import it.polito.wa2.server.categories.CategoryService
import it.polito.wa2.server.categories.ProductCategory
import it.polito.wa2.server.items.ItemDTO
import it.polito.wa2.server.items.toDTO
import it.polito.wa2.server.security.WebSecurityConfig
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@Observed
class ProfileServiceImpl(
    private val categoryRepository: CategoryRepository,
    private val addressRepository: AddressRepository,
    private val profileRepository: ProfileRepository,
    private val categoryService: CategoryService,
    private val addressService: AddressService
) : ProfileService {


    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    @PostAuthorize(
        "" +
                "(returnObject.email == #loggedEmail || hasRole('manager')) || " +
                "((returnObject.role == T(it.polito.wa2.server.profiles.ProfileRole).CLIENT.toString()) && " +
                "(hasRole('expert'))) || " +
                "((returnObject.role == T(it.polito.wa2.server.profiles.ProfileRole).EXPERT.toString()) && " +
                "(hasRole('expert') || hasRole('client')))"
    )
    override fun getProfile(email: String, loggedEmail: String): ProfileDTO {
        val profile = getProfilePrivate(email)
        return profile.toDTO()
    }

    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    override fun getProfileInfo(email: String): ProfileDTO {
        return profileRepository.findByEmail(email)?.toDTO()
            ?: throw ProfileNotFoundException("Profile with email '${email}' not found")
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('${WebSecurityConfig.MANAGER}', '${WebSecurityConfig.CLIENT}')")
    override fun getProfileItems(email: String): List<ItemDTO> {
        val profile = profileRepository.findByEmail(email)
            ?: throw ProfileNotFoundException("Profile with email '${email}' not found")
        return profile.items.map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('${WebSecurityConfig.MANAGER}')")
    override fun getExpertByCategory(category: ProductCategory): List<ProfileDTO> {
        return profileRepository
            .findByRole(ProfileRole.EXPERT)
            .filter { it.expertCategories.any { cat -> cat.name == category } }
            .map { it.toDTO() }
    }

    override fun addProfileWithRole(profileDTO: ProfileDTO, profileRole: ProfileRole) {
        if (profileRepository.findByEmail(profileDTO.email) != null)
            throw DuplicateProfileException("Profile with email '${profileDTO.email}' already exists")
        val profile = profileDTO.toNewProfile(profileRole)

        if (profileRole == ProfileRole.EXPERT)
            profile.expertCategories = profileDTO.expertCategories!!.map { getCategoryByName(it) }.toMutableSet()

        profileRepository.save(profile)
        if (profileRole == ProfileRole.CLIENT) {
            addressService.addAddress(profile.email, profileDTO.address!!)
            profile.address = addressRepository.findByClient(profile)!!
            profileRepository.save(profile)
        }
    }

    @PreAuthorize("isAuthenticated()")
    override fun updateProfile(email: String, newProfileDTO: ProfileDTO) {
        if (email != newProfileDTO.email)
            throw BadRequestProfileException("Email in path doesn't match the email in the body")
        val profile = profileRepository.findByEmail(email)
            ?: throw ProfileNotFoundException("Profile with email '${email}' not found")
        if (profile.role != ProfileRole.EXPERT && newProfileDTO.expertCategories!!.isNotEmpty())
            throw UnprocessableProfileException("Only the experts can be assigned to categories")

        profile.email = newProfileDTO.email
        profile.name = newProfileDTO.name
        profile.surname = newProfileDTO.surname
        when (profile.role) {
            ProfileRole.EXPERT -> profile.expertCategories =
                newProfileDTO.expertCategories!!.map { getCategoryByName(it) }.toMutableSet()

            ProfileRole.CLIENT -> {
                val clientPreviousAddressDTO = addressService.getAddressOfClient(profile.email)
                if (clientPreviousAddressDTO == null)
                    addressService.addAddress(profile.email, newProfileDTO.address!!)
                else
                    addressService.updateAddressOfClient(profile.email, newProfileDTO.address!!)
            }

            else -> {}
        }
        profileRepository.save(profile)
    }

    override fun getIsValid(email: String): Boolean? {
        return profileRepository.findByEmail(email)?.valid
    }

    override fun validateProfile(email: String) {
        val profile = profileRepository.findByEmail(email)
            ?: throw ProfileNotFoundException("User with email $email not found")
        profile.valid = true
        profileRepository.save(profile)
    }

    private fun getCategoryByName(categoryName: ProductCategory): Category {
        val categoryDTO = categoryService.getCategory(categoryName)
        return categoryRepository.findByName(categoryDTO.categoryName)!!
    }

    private fun getProfilePrivate(email: String): Profile {
        return profileRepository.findByEmail(email)
            ?: throw ProfileNotFoundException("Profile with email '${email}' not found")
    }

}