package it.polito.wa2.server.ticketing.message

import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.profiles.ProfileDTO
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.toDTO
import it.polito.wa2.server.profiles.toProfile
import it.polito.wa2.server.ticketing.attachment.AttachmentDTO
import it.polito.wa2.server.ticketing.attachment.AttachmentRepository
import it.polito.wa2.server.ticketing.attachment.toAttachment
import it.polito.wa2.server.ticketing.attachment.toDTO
import it.polito.wa2.server.ticketing.ticket.TicketDTO
import it.polito.wa2.server.ticketing.ticket.TicketRepository
import it.polito.wa2.server.ticketing.ticket.toDTO
import it.polito.wa2.server.ticketing.ticket.toTicket
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.data.repository.findByIdOrNull
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