package it.polito.wa2.server.profiles

import org.springframework.stereotype.Service

@Service
class ProfileServiceImpl(
        private val profileRepository: ProfileRepository
): ProfileService {

    override fun getProfile(email: String): ProfileDTO? {
        return profileRepository.findByEmail(email)
            ?.toDTO()
    }

    override fun addProfile(profile: ProfileDTO) {
        profileRepository.save(profile.toProfile())
   }

    override fun updateProfile(email: String, newProfile: ProfileDTO) {
        val profile = profileRepository.findByEmail(email)
        if(profile != null) {
            profile.email = newProfile.email
            profile.name = newProfile.name
            profile.surname = newProfile.surname
            profileRepository.save(profile)
        }
    }
}