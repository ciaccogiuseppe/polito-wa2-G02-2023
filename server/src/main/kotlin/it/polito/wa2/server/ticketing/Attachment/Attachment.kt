package it.polito.wa2.server.ticketing.Attachment

import it.polito.wa2.server.ticketing.Message.Message
import jakarta.persistence.*
import org.hibernate.annotations.Type


@Entity
@Table(name="attachment")
class Attachment {

    var name : String = ""

    @Lob
    @Column(columnDefinition="bytea")
    var attachment: ByteArray? = null

    @ManyToOne
    var message_id : Message? = null

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attachment_generator")
    @SequenceGenerator(name = "attachment_generator",
        sequenceName = "attachment_id_seq",
        initialValue = 1,
        allocationSize = 1
    )
    var attachment_id : Long? = null
}