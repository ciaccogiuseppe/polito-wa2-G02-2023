package it.polito.wa2.server.ticketing.message


import it.polito.wa2.server.TicketNotFoundException
import it.polito.wa2.server.ticketing.ticket.TicketRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class MessageServiceImpl(private val messageRepository: MessageRepository, private val ticketRepository: TicketRepository): MessageService {
    override fun getChat(ticketId: String): List<MessageDTO> {
        val ticket = ticketRepository.findByIdOrNull(ticketId.toLong())
            ?: throw TicketNotFoundException("Ticket with id '${ticketId}' not found")
        return messageRepository.findAllByTicket(ticket).map {it.toDTO()}
    }

    override fun addMessage(ticketId: String, message: MessageDTO) {
        val ticket = ticketRepository.findByIdOrNull(ticketId.toLong())
            ?: throw TicketNotFoundException("Ticket with id '${ticketId}' not found")
        ticket.messages.add(message.toMessage())
        ticketRepository.save(ticket)
    }

}