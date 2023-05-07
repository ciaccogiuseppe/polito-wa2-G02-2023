package it.polito.wa2.server.ticketing.message

import it.polito.wa2.server.profiles.ProfileDTO
import it.polito.wa2.server.profiles.toDTO
import it.polito.wa2.server.profiles.toProfile
import it.polito.wa2.server.ticketing.attachment.AttachmentDTO
import it.polito.wa2.server.ticketing.attachment.toAttachment
import it.polito.wa2.server.ticketing.attachment.toDTO
import it.polito.wa2.server.ticketing.ticket.TicketDTO
import it.polito.wa2.server.ticketing.ticket.toDTO
import it.polito.wa2.server.ticketing.ticket.toTicket
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.sql.Timestamp

data class MessageDTO(
    val messageId : Long?,
    @field:NotNull
    val ticket: TicketDTO?,
    val sender: ProfileDTO?,
    @field:NotBlank(message="a message text is mandatory")
    val text: String,
    val sentTimestamp: Timestamp?,
    val attachments: MutableSet<AttachmentDTO>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageDTO

        if (messageId != other.messageId) return false
        /*if (attachment != null) {
            if (other.attachment == null) return false
            if (!attachment.contentEquals(other.attachment)) return false
        } else if (other.attachment != null) return false
        if (name != other.name) return false*/

        return true
    }

    override fun hashCode(): Int {
        var result = messageId?.hashCode() ?: 0
        /*result = 31 * result + (attachment?.contentHashCode() ?: 0)
        result = 31 * result + name.hashCode()*/
        return result
    }
}

fun Message.toDTO(): MessageDTO {
    return MessageDTO(messageId, ticket?.toDTO(), sender?.toDTO(),
        text, sentTimestamp, attachments.map{it.toDTO()}.toMutableSet())
}

fun MessageDTO.toMessage(): Message {
    val message = Message()
    message.attachments = attachments.map{it.toAttachment()}.toMutableSet()
    message.messageId = messageId
    message.text = text
    message.sender = sender?.toProfile()
    message.ticket = ticket?.toTicket()
    message.sentTimestamp = sentTimestamp
    return message
}