package it.polito.wa2.server.ticketing.message

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.BadRequestMessageException
import it.polito.wa2.server.ForbiddenException
import it.polito.wa2.server.UnauthorizedMessageException
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileRole
import it.polito.wa2.server.profiles.ProfileService
import it.polito.wa2.server.security.WebSecurityConfig
import it.polito.wa2.server.ticketing.attachment.Attachment
import it.polito.wa2.server.ticketing.attachment.AttachmentDTO
import it.polito.wa2.server.ticketing.attachment.AttachmentRepository
import it.polito.wa2.server.ticketing.attachment.AttachmentService
import it.polito.wa2.server.ticketing.ticket.Ticket
import it.polito.wa2.server.ticketing.ticket.TicketRepository
import it.polito.wa2.server.ticketing.ticket.TicketService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service @Transactional @Observed
class MessageServiceImpl(
    private val messageRepository: MessageRepository,
    private val ticketRepository: TicketRepository,
    private val profileRepository: ProfileRepository,
    private val attachmentRepository: AttachmentRepository,
    private val profileService: ProfileService,
    private val ticketService: TicketService,
    private val attachmentService: AttachmentService): MessageService {
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('${WebSecurityConfig.CLIENT}', '${WebSecurityConfig.EXPERT}')")
    override fun getChat(ticketId: Long, userEmail: String): List<MessageDTO> {
        val user = getProfileByEmail(userEmail)
        val ticket = if(user.role == ProfileRole.CLIENT) getTicketClient(ticketId, userEmail)
            else getTicketExpert(ticketId, userEmail)
        checkSender(user, ticket)
        return messageRepository.findAllByTicket(ticket).map {it.toDTO()}
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('${WebSecurityConfig.MANAGER}')")
    override fun getChatManager(ticketId: Long, userEmail: String): List<MessageDTO> {
        val ticket = getTicketManager(ticketId, userEmail)
        return messageRepository.findAllByTicket(ticket).map {it.toDTO()}
    }

    @PreAuthorize("hasAnyRole('${WebSecurityConfig.CLIENT}', '${WebSecurityConfig.EXPERT}')")
    override fun addMessageSender(ticketId: Long, messageDTO: MessageDTO, userEmail: String) {
        if(ticketId != messageDTO.ticketId)
            throw BadRequestMessageException("The ticket ids are different")
        val user = getProfileByEmail(userEmail)
        val ticket = if(user.role == ProfileRole.CLIENT) getTicketClient(ticketId, userEmail)
            else getTicketExpert(ticketId, userEmail)
        checkSender(user, ticket)
        addMessage(messageDTO, userEmail, ticket)
    }

    @PreAuthorize("hasRole('${WebSecurityConfig.MANAGER}')")
    override fun addMessageManager(ticketId: Long, messageDTO: MessageDTO, userEmail: String) {
        if(ticketId != messageDTO.ticketId)
            throw BadRequestMessageException("The ticket ids are different")
        val ticket = getTicketManager(ticketId, userEmail)
       addMessage(messageDTO, userEmail, ticket)
    }

    private fun getTicketManager(ticketId: Long, userEmail: String): Ticket {
        val ticketDTO = ticketService.managerGetTicket(ticketId, userEmail)
        return ticketRepository.findByIdOrNull(ticketDTO.ticketId)!!
    }

    private fun getTicketClient(ticketId: Long, userEmail: String): Ticket {
        val ticketDTO = ticketService.clientGetTicket(ticketId, userEmail)
        return ticketRepository.findByIdOrNull(ticketDTO.ticketId)!!
    }

    private fun getTicketExpert(ticketId: Long, userEmail: String): Ticket {
        val ticketDTO = ticketService.expertGetTicket(ticketId, userEmail)
        return ticketRepository.findByIdOrNull(ticketDTO.ticketId)!!
    }

    private fun getAttachment(attachmentDTO: AttachmentDTO): Attachment {
        var attachmentId = attachmentDTO.attachmentId
        if(attachmentId == null)
            attachmentId = attachmentService.addAttachment(attachmentDTO)
        return attachmentRepository.findByIdOrNull(attachmentId)!!
    }



    private fun getProfileByEmail(email: String): Profile {
        val profileDTO = profileService.getProfile(email)
        return profileRepository.findByEmail(profileDTO.email)!!
    }

    private fun addMessage(messageDTO: MessageDTO, userEmail: String, ticket: Ticket){
        val attachments = messageDTO.attachments.map{getAttachment(it)}.toMutableSet()
        val sender = getProfileByEmail(userEmail)
        if(sender != ticket.client && (ticket.expert != null && ticket.expert != sender))
            throw UnauthorizedMessageException("Sender is not related to ticket")
        val message = messageDTO.toNewMessage(attachments, sender, ticket)
        messageRepository.save(message)
        ticket.messages.add(message)
        ticketRepository.save(ticket)
    }

    private fun checkSender(user: Profile, ticket: Ticket) {
        val clientOfTicket = ticket.client!!
        val expertOfTicket = ticket.expert!!
        if(user != clientOfTicket && user != expertOfTicket)
            throw ForbiddenException("User is not related to ticket")
    }
}