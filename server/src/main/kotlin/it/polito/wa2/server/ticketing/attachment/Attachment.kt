package it.polito.wa2.server.ticketing.attachment

import it.polito.wa2.server.ticketing.message.Message
import jakarta.persistence.*


@Entity
@Table(name="attachments")
class Attachment {

    var name : String = ""

    @Lob
    @Column(columnDefinition="bytea")
    var attachment: ByteArray? = null

    @ManyToOne
    @JoinColumn(name="message_id")
    var message : Message? = null

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attachment_generator")
    @SequenceGenerator(name = "attachment_generator",
        sequenceName = "attachment_id_seq",
        initialValue = 1,
        allocationSize = 1
    )
    var attachmentId : Long? = null
}