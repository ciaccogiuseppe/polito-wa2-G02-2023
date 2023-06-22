package it.polito.wa2.server.ticketing.attachment

import it.polito.wa2.server.EntityBase
import it.polito.wa2.server.ticketing.message.Message
import jakarta.persistence.*


@Entity
@Table(name="attachments")
class Attachment :EntityBase<Long>(){

    @Column(nullable = false)
    var name : String = ""

    @Column(columnDefinition="bytea", nullable = false)
    var attachment: ByteArray = byteArrayOf()

    @ManyToOne
    @JoinColumn(name="message_id")
    var message : Message? = null

}