package it.polito.wa2.server.ticketing.tickethistory


import it.polito.wa2.server.ticketing.ticket.TicketService
import it.polito.wa2.server.ticketing.ticket.toTicket
import org.springframework.stereotype.Service

@Service
class TicketHistoryServiceImpl(
    private val ticketHistoryRepository: TicketHistoryRepository,
    private val ticketService: TicketService
): TicketHistoryService {
    override fun getAllTicketHistory(): List<TicketHistoryDTO> {
        return ticketHistoryRepository.findAll().map {it.toDTO()}
    }

    override fun getTicketHistory(ticketId: Long): List<TicketHistoryDTO> {
        val ticket = ticketService.getTicket(ticketId).toTicket()
        return ticketHistoryRepository.findAllByTicket(ticket).map {it.toDTO()}
    }

}