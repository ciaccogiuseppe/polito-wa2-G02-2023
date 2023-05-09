package it.polito.wa2.server.ticketing.message

import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.profiles.ProfileDTO
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.toDTO
//import it.polito.wa2.server.profiles.toProfile
import it.polito.wa2.server.ticketing.attachment.AttachmentDTO
import it.polito.wa2.server.ticketing.attachment.AttachmentRepository
// import it.polito.wa2.server.ticketing.attachment.toAttachment
import it.polito.wa2.server.ticketing.attachment.toDTO
import it.polito.wa2.server.ticketing.ticket.TicketDTO
import it.polito.wa2.server.ticketing.ticket.TicketRepository
import it.polito.wa2.server.ticketing.ticket.toDTO
//import it.polito.wa2.server.ticketing.ticket.toTicket
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import org.springframework.data.repository.findByIdOrNull
import java.sql.Timestamp

data class MessageDTO(
    @field:Positive
    val messageId : Long?,
    @field:Positive
    val ticketId: Long,
    @field:Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$",
        message="email must be valid")
    val sender: String?,
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
        if (ticketId != other.ticketId) return false
        if (sender != other.sender) return false
        if (text != other.text) return false
        if (sentTimestamp != other.sentTimestamp) return false
        if (attachments != other.attachments) return false

        return true
    }

    override fun hashCode(): Int {
        var result = messageId?.hashCode() ?: 0
        result = 31 * result + ticketId.hashCode()
        result = 31 * result + (sender?.hashCode() ?: 0)
        result = 31 * result + text.hashCode()
        result = 31 * result + (sentTimestamp?.hashCode() ?: 0)
        result = 31 * result + attachments.hashCode()
        return result
    }
}

fun Message.toDTO(): MessageDTO {
    return MessageDTO(messageId, ticket?.ticketId!!, sender?.email,
        text, sentTimestamp, attachments.map{it.toDTO()}.toMutableSet())
}

/*
fun MessageDTO.toMessage(
    messageRepository: MessageRepository,
    profileRepository: ProfileRepository,
    attachmentRepository: AttachmentRepository,
    ticketRepository: TicketRepository,
    productRepository: ProductRepository,
    ): Message {
    var message = messageRepository.findByIdOrNull(messageId.toString())
    if(message!=null)
        return message
    message = Message()
    message.attachments = attachments.map{it.toAttachment(attachmentRepository)}.toMutableSet()
    message.messageId = messageId
    message.text = text
    message.sender = sender?.toProfile(profileRepository)
    message.ticket = ticket?.toTicket(ticketRepository, productRepository, profileRepository)
    message.sentTimestamp = sentTimestamp
    return message
}
*/
