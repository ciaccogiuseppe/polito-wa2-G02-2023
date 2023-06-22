package it.polito.wa2.server.ticketing.passwordReset

import it.polito.wa2.server.EntityBase
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.ticketing.attachment.Attachment
import it.polito.wa2.server.ticketing.ticket.Ticket
import jakarta.persistence.*
import java.sql.Timestamp
import java.util.*

@Entity
@Table(name="passwordreset")
class PasswordReset {

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    var profile : Profile? = null

    @Id
    var uuid: UUID? = null


    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    var created : Timestamp? = null
}