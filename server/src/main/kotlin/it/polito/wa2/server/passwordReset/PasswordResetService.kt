package it.polito.wa2.server.passwordReset

import java.util.*

interface PasswordResetService {
    fun checkIsValid(email: String, uuid: UUID): Boolean
    fun delete(uuid: UUID)

    fun addPasswordReset(email: String, uuid: UUID)
}