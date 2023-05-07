package it.polito.wa2.server.ticketing.tickethistory

import it.polito.wa2.server.ticketing.ticket.Ticket
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface TicketHistoryRepository: JpaRepository<TicketHistory, String> {
    fun findByTicket(ticket: Ticket): List<TicketHistory>
}