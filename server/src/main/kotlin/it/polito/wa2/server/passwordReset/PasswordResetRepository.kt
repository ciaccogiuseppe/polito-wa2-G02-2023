package it.polito.wa2.server.passwordReset

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PasswordResetRepository : JpaRepository<PasswordReset, UUID>