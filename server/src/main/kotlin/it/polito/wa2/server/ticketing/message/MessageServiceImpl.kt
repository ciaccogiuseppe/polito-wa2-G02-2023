package it.polito.wa2.server.ticketing.message

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
    override fun getChat(ticketId: Long): List<MessageDTO> {
        val ticket = getTicket(ticketId)
        return messageRepository.findAllByTicket(ticket).map {it.toDTO()}
    }

    override fun addMessage(ticketId: Long, message: MessageDTO) {
        val ticket = getTicket(ticketId)
        val attachments = message.attachments.map{getAttachment(it)}.toMutableSet()
        val sender = getProfile(1)
        ticket.messages.add(message.toNewMessage(attachments, sender, ticket))
        ticketRepository.save(ticket)
    }

    private fun getTicket(ticketId: Long): Ticket {
        val ticketDTO = ticketService.getTicket(ticketId)
        return ticketRepository.findByIdOrNull(ticketDTO.ticketId)!!
    }

    private fun getAttachment(attachment: AttachmentDTO): Attachment {
        var attachmentId = attachment.attachmentId
        if(attachmentId == null)
            attachmentId = attachmentService.addAttachment(attachment)
        val attachmentDTO = attachmentService.getAttachment(attachmentId)
        return attachmentRepository.findByIdOrNull(attachmentDTO.attachmentId)!!
    }

    private fun getProfile(profileId: Long): Profile {
        val profileDTO = profileService.getProfileById(profileId)
        return profileRepository.findByEmail(profileDTO.email)!!
    }
}