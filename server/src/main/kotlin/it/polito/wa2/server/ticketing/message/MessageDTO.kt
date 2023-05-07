package it.polito.wa2.server.ticketing.message

import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.ticketing.attachment.Attachment
import it.polito.wa2.server.ticketing.ticket.Ticket
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.sql.Timestamp

data class MessageDTO(
    val messageId : Long?,
    @field:NotNull
    val ticket: Ticket?,
    val sender: Profile?,
    @field:NotBlank(message="a message text is mandatory")
    val text: String,
    val sentTimestamp: Timestamp?,
    val attachments: MutableSet<Attachment>,
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
    return MessageDTO(messageId, ticket, sender, text, sentTimestamp, attachments)
}

fun MessageDTO.toMessage(): Message {
    val message = Message()
    message.attachments = attachments
    message.messageId = messageId
    message.text = text
    message.sender = sender
    message.ticket = ticket
    message.sentTimestamp = sentTimestamp
    return message
}