package it.polito.wa2.server.profiles

interface ProfileService {
    fun getProfile(email: String): ProfileDTO

    fun getProfileById(id: Long): ProfileDTO

    fun addProfile(profileDTO: ProfileDTO)

    fun updateProfile(email: String, newProfileDTO: ProfileDTO)
}