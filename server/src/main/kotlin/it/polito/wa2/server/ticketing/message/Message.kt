package it.polito.wa2.server.ticketing.message

import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.ticketing.attachment.Attachment
import it.polito.wa2.server.ticketing.ticket.Ticket
import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name="message")
class Message {

    @ManyToOne
    var ticketId : Ticket? = null

    @ManyToOne
    var senderId : Profile? = null

    @OneToMany(mappedBy = "messageId")
    var attachments = mutableSetOf<Attachment>()

    var text : String = ""

    @Temporal(TemporalType.TIMESTAMP)
    var timestamp : Timestamp? = null

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "message_generator")
    @SequenceGenerator(name = "message_generator",
        sequenceName = "messages_id_seq",
        initialValue = 1,
        allocationSize = 1
    )
    var messageId : Long? = null
}