package it.polito.wa2.server.passwordReset

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.profiles.ProfileRepository
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
        val user = profileRepository.findByEmail(email) ?: throw NotFoundException("User not found")
        val passwordReset = PasswordReset()
        passwordReset.created = Timestamp(System.currentTimeMillis())
        passwordReset.profile = user
        passwordReset.uuid = uuid
        passwordResetRepository.save(passwordReset)

    }

}