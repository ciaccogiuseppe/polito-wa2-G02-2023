package it.polito.wa2.server.profiles

import it.polito.wa2.server.DuplicateProfileException
import it.polito.wa2.server.ProfileNotFoundException
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
        if (profileRepository.findByEmail(profile.email) != null)
            throw DuplicateProfileException("Profile with email '${profile.email}' already exists")
        profileRepository.save(profile.toProfile())
   }

    override fun updateProfile(email: String, newProfile: ProfileDTO) {
        if(profileRepository.findByEmail(newProfile.email) != null)
            throw DuplicateProfileException("Profile with email '${newProfile.email}' already exists")
        val profile = profileRepository.findByEmail(email)
        if(profile != null) {
            profile.email = newProfile.email
            profile.name = newProfile.name
            profile.surname = newProfile.surname
            profileRepository.save(profile)
        } else {
            throw ProfileNotFoundException("Profile with email '${email}' not found")
        }
    }
}