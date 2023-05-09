package it.polito.wa2.server.ticketing.message


import it.polito.wa2.server.TicketNotFoundException
import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileService
import it.polito.wa2.server.ticketing.attachment.AttachmentRepository
import it.polito.wa2.server.ticketing.ticket.TicketRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class MessageServiceImpl(
    private val messageRepository: MessageRepository,
    private val ticketRepository: TicketRepository,
    private val profileRepository: ProfileRepository,
    private val attachmentRepository: AttachmentRepository,
    private val productRepository: ProductRepository,
    private val profileService: ProfileService): MessageService {
    override fun getChat(ticketId: Long): List<MessageDTO> {
        val ticket = ticketRepository.findByIdOrNull(ticketId)
            ?: throw TicketNotFoundException("Ticket with id '${ticketId}' not found")
        return messageRepository.findAllByTicket(ticket).map {it.toDTO()}
    }

    override fun addMessage(ticketId: Long, message: MessageDTO) {
        val ticket = ticketRepository.findByIdOrNull(ticketId)
            ?: throw TicketNotFoundException("Ticket with id '${ticketId}' not found")
        val attachments = message.attachments.map{attachmentRepository.findByIdOrNull(it.attachmentId)!!}.toMutableSet()
        val repProfileId = profileService.getProfileById(1).email
        val sender = profileRepository.findByEmail(repProfileId)!!
        ticket.messages.add(message.toNewMessage(attachments, sender, ticket))
        ticketRepository.save(ticket)
    }

}