package it.polito.wa2.server.emailVerification

import it.polito.wa2.server.EntityBase
import it.polito.wa2.server.profiles.Profile
import jakarta.persistence.*
import java.sql.Timestamp
import java.util.*

@Entity
@Table(name = "email_verification")
class EmailVerification : EntityBase<Long>() {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    var profile: Profile? = null

    var uuid: UUID? = null


    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    var created: Timestamp? = null
}