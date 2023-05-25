package it.polito.wa2.server.ticketing.message

import it.polito.wa2.server.BadRequestMessageException
import it.polito.wa2.server.UnauthorizedMessageException
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileService
import it.polito.wa2.server.ticketing.attachment.Attachment
import it.polito.wa2.server.ticketing.attachment.AttachmentDTO
import it.polito.wa2.server.ticketing.attachment.AttachmentRepository
import it.polito.wa2.server.ticketing.attachment.AttachmentService
import it.polito.wa2.server.ticketing.ticket.Ticket
import it.polito.wa2.server.ticketing.ticket.TicketRepository
import it.polito.wa2.server.ticketing.ticket.TicketService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service @Transactional
class MessageServiceImpl(
    private val messageRepository: MessageRepository,
    private val ticketRepository: TicketRepository,
    private val profileRepository: ProfileRepository,
    private val attachmentRepository: AttachmentRepository,
    private val profileService: ProfileService,
    private val ticketService: TicketService,
    private val attachmentService: AttachmentService): MessageService {
    @Transactional(readOnly = true)
    override fun getChat(ticketId: Long, userEmail: String): List<MessageDTO> {
        val ticket = getTicket(ticketId, userEmail)
        return messageRepository.findAllByTicket(ticket).map {it.toDTO()}
    }

    override fun addMessage(ticketId: Long, messageDTO: MessageDTO, userEmail: String) {
        if(ticketId != messageDTO.ticketId)
            throw BadRequestMessageException("The ticket ids are different")
        val ticket = getTicket(ticketId, userEmail)
        val attachments = messageDTO.attachments.map{getAttachment(it, userEmail)}.toMutableSet()
        val sender = getProfile(messageDTO.senderId)
        if(sender != ticket.customer && (ticket.expert != null && ticket.expert != sender))
            throw UnauthorizedMessageException("Sender is not related to ticket")
        val message = messageDTO.toNewMessage(attachments, sender, ticket)
        messageRepository.save(message)
        ticket.messages.add(message)
        ticketRepository.save(ticket)

    }

    private fun getTicket(ticketId: Long, userEmail: String): Ticket {
        val ticketDTO = ticketService.getTicket(ticketId, userEmail)
        return ticketRepository.findByIdOrNull(ticketDTO.ticketId)!!
    }

    private fun getAttachment(attachmentDTO: AttachmentDTO, userEmail: String): Attachment {
        var attachmentId = attachmentDTO.attachmentId
        if(attachmentId == null)
            attachmentId = attachmentService.addAttachment(attachmentDTO)
        val newAttachmentDTO = attachmentService.getAttachment(attachmentId, userEmail)
        return attachmentRepository.findByIdOrNull(newAttachmentDTO.attachmentId)!!
    }

    private fun getProfile(profileId: Long): Profile {
        val profileDTO = profileService.getProfileById(profileId)
        return profileRepository.findByEmail(profileDTO.email)!!
    }
}