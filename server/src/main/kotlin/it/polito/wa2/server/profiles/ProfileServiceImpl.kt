package it.polito.wa2.server.profiles

import org.springframework.stereotype.Service

@Service
class ProfileServiceImpl(
        private val profileRepository: ProfileRepository
): ProfileService {

    override fun getProfile(email: String): ProfileDTO? {
        val found = profileRepository.findByEmail(email)
        return if (found.isEmpty()) null
            else found[0].toDTO()
    }

    override fun addProfile(profile: ProfileDTO) {
        profileRepository.save(profile.toProfile())
   }

    override fun updateProfile(email: String, newProfile: ProfileDTO) {
        val profile = profileRepository.findByEmail(email)[0]
        profile.email = newProfile.email
        profile.name = newProfile.name
        profile.surname = newProfile.surname
        profileRepository.save(profile)
    }
}