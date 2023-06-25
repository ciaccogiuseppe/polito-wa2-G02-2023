package it.polito.wa2.server.emailVerification

import it.polito.wa2.server.profiles.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EmailVerificationRepository : JpaRepository<EmailVerification, UUID>{
    fun findByProfile(profile: Profile) : EmailVerification?
    fun findByUuid(uuid: UUID) : EmailVerification?
    fun deleteAllByUuid(uuid: UUID)
}