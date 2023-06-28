package it.polito.wa2.server.emailVerification

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.passwordReset.PasswordReset
import it.polito.wa2.server.passwordReset.PasswordResetRepository
import it.polito.wa2.server.passwordReset.PasswordResetService
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.util.*
import javax.ws.rs.NotFoundException

@Service
@Transactional
@Observed
class PasswordResetServiceImpl(
    private val passwordResetRepository: PasswordResetRepository,
    private val profileService: ProfileService,
    private val profileRepository: ProfileRepository
) : PasswordResetService {
    override fun checkIsValid(email: String, uuid: UUID): Boolean {
        val record = passwordResetRepository.findById(uuid)
        val curTimestamp = Timestamp(System.currentTimeMillis() - 43200000)
        if (!record.isPresent) return false
        else
            if (record.get().created!!.before(curTimestamp)) {
                passwordResetRepository.deleteById(uuid)
                return false
            }
        return email == record.get().profile!!.email
    }

    override fun delete(uuid: UUID) {
        passwordResetRepository.deleteById(uuid)
    }

    override fun addPasswordReset(email: String, uuid: UUID) {
        val user = getProfileByEmail(email)
            ?: throw NotFoundException("User not found")
        val passwordReset = PasswordReset()
        passwordReset.created = Timestamp(System.currentTimeMillis())
        passwordReset.profile = user
        passwordReset.uuid = uuid
        passwordResetRepository.save(passwordReset)

    }

    private fun getProfileByEmail(email: String): Profile? {
        val profileDTO = profileService.getProfile(email, email)
        return profileRepository.findByEmail(profileDTO.email)
    }

}