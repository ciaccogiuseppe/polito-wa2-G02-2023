package it.polito.wa2.server.emailVerification

import java.util.*

interface EmailVerificationService {
    fun checkIsValid(uuid: UUID): Boolean
    fun getEmail(uuid: UUID): String?
    fun delete(uuid: UUID)

    fun addEmailVerification(email: String, uuid: UUID)
}