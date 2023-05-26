package it.polito.wa2.server.ticketing.message

import it.polito.wa2.server.EntityBase
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.ticketing.attachment.Attachment
import it.polito.wa2.server.ticketing.ticket.Ticket
import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name="messages")
class Message: EntityBase<Long>() {

    @ManyToOne
    @JoinColumn(name="ticket_id", nullable = false)
    var ticket : Ticket? = null

    @ManyToOne
    @JoinColumn(nullable = false)
    var sender : Profile? = null

    @OneToMany(mappedBy = "message")
    var attachments = mutableSetOf<Attachment>()

    @Column(nullable = false)
    var text : String = ""

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    var sentTimestamp : Timestamp? = null
}