package it.polito.wa2.server.ticketing.attachment

import it.polito.wa2.server.ticketing.message.Message
import jakarta.persistence.*


@Entity
@Table(name="attachments")
class Attachment {

    @Column(nullable = false)
    var name : String = ""

    //@Lob
    @Column(columnDefinition="bytea", nullable = false)
    var attachment: ByteArray = byteArrayOf()

    @ManyToOne
    @JoinColumn(name="message_id", nullable = false)
    var message : Message? = null

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attachment_generator")
    @SequenceGenerator(name = "attachment_generator",
        sequenceName = "attachments_id_seq",
        initialValue = 1,
        allocationSize = 1
    )
    @Column(updatable = false, nullable = false)
    var attachmentId : Long? = null
}