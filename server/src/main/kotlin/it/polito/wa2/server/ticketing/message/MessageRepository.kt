package it.polito.wa2.server.ticketing.message

import it.polito.wa2.server.ticketing.ticket.Ticket
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository: JpaRepository<Message, String> {
    fun findAllByTicketId(ticketId: Ticket): List<MessageDTO>
}