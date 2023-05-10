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

    override fun addProfile(profileDTO: ProfileDTO) {
        if (profileRepository.findByEmail(profileDTO.email) != null)
            throw DuplicateProfileException("Profile with email '${profileDTO.email}' already exists")
        profileRepository.save(profileDTO.toNewProfile())
   }

    override fun updateProfile(email: String, newProfileDTO: ProfileDTO) {
        val profile = profileRepository.findByEmail(email)
            ?: throw ProfileNotFoundException("Profile with email '${email}' not found")
        if(email != newProfileDTO.email && profileRepository.findByEmail(newProfileDTO.email) != null)
            throw DuplicateProfileException("Profile with email '${newProfileDTO.email}' already exists")
        profile.email = newProfileDTO.email
        profile.name = newProfileDTO.name
        profile.surname = newProfileDTO.surname
        profileRepository.save(profile)
    }
}