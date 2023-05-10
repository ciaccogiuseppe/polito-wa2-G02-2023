package it.polito.wa2.server.ticketing.message

import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.ticketing.attachment.Attachment
import it.polito.wa2.server.ticketing.attachment.AttachmentDTO
import it.polito.wa2.server.ticketing.attachment.toDTO
import it.polito.wa2.server.ticketing.ticket.Ticket
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import java.sql.Timestamp
import java.time.LocalDateTime

data class MessageDTO(
    @field:Positive
    val messageId : Long?,
    @field:Positive
    val ticketId: Long,
    @field:Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$",
        message="email must be valid")
    val senderId: String,
    @field:NotBlank(message="a message text is mandatory")
    val text: String,
    val sentTimestamp: Timestamp?,
    val attachments: MutableSet<AttachmentDTO>,
)

fun Message.toDTO(): MessageDTO {
    return MessageDTO(messageId, ticket?.ticketId!!, sender!!.email,
        text, sentTimestamp, attachments.map{it.toDTO()}.toMutableSet())
}

fun MessageDTO.toNewMessage(
    attachments: MutableSet<Attachment>,
    sender: Profile,
    ticket: Ticket): Message {
    val message = Message()
    message.attachments = attachments
    message.text = text
    message.sender = sender
    message.ticket = ticket
    message.sentTimestamp = Timestamp.valueOf(LocalDateTime.now())
    return message
}
