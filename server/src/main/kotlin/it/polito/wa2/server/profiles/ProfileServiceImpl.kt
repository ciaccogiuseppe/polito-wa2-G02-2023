package it.polito.wa2.server.profiles

import it.polito.wa2.server.DuplicateProfileException
import it.polito.wa2.server.ProfileNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service @Transactional
class ProfileServiceImpl(
        private val profileRepository: ProfileRepository
): ProfileService {

    @Transactional(readOnly = true)
    override fun getProfile(email: String): ProfileDTO {
        return profileRepository.findByEmail(email)?.toDTO()
            ?: throw ProfileNotFoundException("Profile with email '${email}' not found")
    }

    @Transactional(readOnly = true)
    override fun getProfileById(id: Long): ProfileDTO {
        return profileRepository.findByIdOrNull(id)?.toDTO()
            ?: throw ProfileNotFoundException("Profile not found")
    }

    override fun addProfile(profile: ProfileDTO) {
        if (profileRepository.findByEmail(profile.email) != null)
            throw DuplicateProfileException("Profile with email '${profile.email}' already exists")
        profileRepository.save(profile.toProfile())
   }

    override fun updateProfile(email: String, newProfile: ProfileDTO) {
        val profile = profileRepository.findByEmail(email)
            ?: throw ProfileNotFoundException("Profile with email '${email}' not found")
        if(email != newProfile.email && profileRepository.findByEmail(newProfile.email) != null)
            throw DuplicateProfileException("Profile with email '${newProfile.email}' already exists")
        profile.email = newProfile.email
        profile.name = newProfile.name
        profile.surname = newProfile.surname
        profileRepository.save(profile)
    }
}