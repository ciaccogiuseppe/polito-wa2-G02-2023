package it.polito.wa2.server.profiles

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.BadRequestProfileException
import it.polito.wa2.server.DuplicateProfileException
import it.polito.wa2.server.ProfileNotFoundException
import it.polito.wa2.server.UnprocessableProfileException
import it.polito.wa2.server.addresses.*
import it.polito.wa2.server.categories.Category
import it.polito.wa2.server.categories.CategoryRepository
import it.polito.wa2.server.categories.CategoryService
import it.polito.wa2.server.categories.ProductCategory
import it.polito.wa2.server.items.ItemDTO
import it.polito.wa2.server.items.toDTO
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service @Transactional @Observed
class ProfileServiceImpl(
    private val categoryRepository: CategoryRepository,
    private val addressRepository: AddressRepository,
    private val profileRepository: ProfileRepository,
    private val categoryService: CategoryService,
    private val addressService: AddressService
): ProfileService {

    @Transactional(readOnly = true)
    override fun getProfile(email: String): ProfileDTO {
        return profileRepository.findByEmail(email)?.toDTO()
            ?: throw ProfileNotFoundException("Profile with email '${email}' not found")
    }

    @Transactional(readOnly = true)
    override fun getProfileById(profileId: Long): ProfileDTO {
        return profileRepository.findByIdOrNull(profileId)?.toDTO()
            ?: throw ProfileNotFoundException("Profile not found")
    }

    override fun getProfileItems(email: String): List<ItemDTO> {
        val profile = profileRepository.findByEmail(email)
            ?: throw ProfileNotFoundException("Profile with email '${email}' not found")
        return profile.items.map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    override fun getExpertByCategory(category: String): List<ProfileDTO> {
        return profileRepository
            .findByRole(ProfileRole.EXPERT)
            .filter{ it.expertCategories.any { cat -> cat.name.toString() == category } }
            .map { it.toDTO() }
    }

    override fun addProfile(profileDTO: ProfileDTO) {
        if (profileRepository.findByEmail(profileDTO.email) != null)
            throw DuplicateProfileException("Profile with email '${profileDTO.email}' already exists")

        profileRepository.save(profileDTO.toNewProfile(ProfileRole.CLIENT))
        addressService.addAddress(profileDTO.email, profileDTO.address!!)
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

    override fun updateProfile(email: String, newProfileDTO: ProfileDTO) {
        val profile = profileRepository.findByEmail(email)
            ?: throw ProfileNotFoundException("Profile with email '${email}' not found")
        if (email != newProfileDTO.email)
            throw BadRequestProfileException("Email in path doesn't match the email in the body")
        if (profile.role != ProfileRole.EXPERT && newProfileDTO.expertCategories!!.isNotEmpty())
            throw UnprocessableProfileException("Only the experts can be assigned to categories")

        profile.email = newProfileDTO.email
        profile.name = newProfileDTO.name
        profile.surname = newProfileDTO.surname
        when (profile.role) {
            ProfileRole.EXPERT -> profile.expertCategories = newProfileDTO.expertCategories!!.map { getCategoryByName(it) }.toMutableSet()
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

    private fun getCategoryByName(categoryName: ProductCategory): Category {
        val categoryDTO = categoryService.getCategory(categoryName)
        return categoryRepository.findByName(categoryDTO.categoryName)!!
    }
}