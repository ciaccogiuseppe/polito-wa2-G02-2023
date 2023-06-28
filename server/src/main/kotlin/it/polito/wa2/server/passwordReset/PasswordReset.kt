package it.polito.wa2.server.passwordReset

import it.polito.wa2.server.profiles.Profile
import jakarta.persistence.*
import java.sql.Timestamp
import java.util.*

@Entity
@Table(name = "password_reset")
class PasswordReset {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    var profile: Profile? = null

    @Id
    var uuid: UUID? = null


    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    var created: Timestamp? = null
}