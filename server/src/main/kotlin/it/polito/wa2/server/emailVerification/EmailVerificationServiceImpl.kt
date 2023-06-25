package it.polito.wa2.server.emailVerification

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.profiles.ProfileRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.util.*
import javax.ws.rs.NotFoundException
import kotlin.jvm.optionals.getOrNull

@Service
@Transactional
@Observed
class EmailVerificationServiceImpl(
    private val emailVerificationRepository: EmailVerificationRepository,
    private val profileRepository: ProfileRepository
) : EmailVerificationService {
    override fun checkIsValid(uuid: UUID): Boolean {
        val record = emailVerificationRepository.findByUuid(uuid)
        val curTimestamp = Timestamp(System.currentTimeMillis() - 86400000)
        if (record == null) return false
        else
            if (record.created!!.before(curTimestamp)) {
                emailVerificationRepository.delete(record)
                return false
            }
        return true
    }

    override fun getEmail(uuid: UUID): String? {
        return emailVerificationRepository.findByUuid(uuid)?.profile?.email
    }

    override fun delete(uuid: UUID) {
        emailVerificationRepository.deleteAllByUuid(uuid)
    }

    override fun addEmailVerification(email: String, uuid: UUID) {
        val user = profileRepository.findByEmail(email)
            ?: throw NotFoundException("User not found")

        val alreadyExists = emailVerificationRepository.findByProfile(user)
        if (alreadyExists != null) {
            alreadyExists.created = Timestamp(System.currentTimeMillis())
            alreadyExists.uuid = uuid
            emailVerificationRepository.save(alreadyExists)
        }
        else{
            val emailVerification = EmailVerification()
            emailVerification.created = Timestamp(System.currentTimeMillis())
            emailVerification.profile = user
            emailVerification.uuid = uuid
            emailVerificationRepository.save(emailVerification)
        }

    }

}