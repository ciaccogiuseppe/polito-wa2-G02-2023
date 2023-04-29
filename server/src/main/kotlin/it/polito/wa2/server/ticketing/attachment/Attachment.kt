package it.polito.wa2.server.ticketing.attachment

import it.polito.wa2.server.ticketing.message.Message
import jakarta.persistence.*


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