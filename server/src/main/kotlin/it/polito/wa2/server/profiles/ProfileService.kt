package it.polito.wa2.server.profiles

interface ProfileService {
    fun getProfile(email: String): ProfileDTO?

    fun addProfile(profile: ProfileDTO)

    fun updateProfile(email: String, newProfile: ProfileDTO)
}