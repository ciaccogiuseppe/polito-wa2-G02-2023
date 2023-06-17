package it.polito.wa2.server.profiles

interface ProfileService {
    fun getProfile(email: String): ProfileDTO

    fun getExpertByCategory(category: String): List<ProfileDTO>

    fun getProfileById(profileId: Long): ProfileDTO

    fun addProfile(profileDTO: ProfileDTO)

    fun addProfileWithRole(profileDTO: ProfileDTO, profileRole: ProfileRole)

    fun updateProfile(email: String, newProfileDTO: ProfileDTO)
}