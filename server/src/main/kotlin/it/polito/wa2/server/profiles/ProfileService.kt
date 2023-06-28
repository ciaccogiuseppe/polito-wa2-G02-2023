package it.polito.wa2.server.profiles

import it.polito.wa2.server.categories.ProductCategory
import it.polito.wa2.server.items.ItemDTO

interface ProfileService {
    fun getProfile(email: String, loggedEmail: String): ProfileDTO

    fun getProfileInfo(email: String): ProfileDTO

    fun getExpertByCategory(category: ProductCategory): List<ProfileDTO>

    fun getProfileItems(email: String): List<ItemDTO>

    fun addProfileWithRole(profileDTO: ProfileDTO, profileRole: ProfileRole)

    fun updateProfile(email: String, newProfileDTO: ProfileDTO)
    fun getIsValid(email: String) : Boolean?
    fun validateProfile(email: String)

}