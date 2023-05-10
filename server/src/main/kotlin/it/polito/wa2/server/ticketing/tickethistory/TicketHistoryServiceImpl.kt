package it.polito.wa2.server.ticketing.tickethistory

import it.polito.wa2.server.ticketing.ticket.Ticket
import it.polito.wa2.server.ticketing.ticket.TicketRepository
import it.polito.wa2.server.ticketing.ticket.TicketService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service @Transactional
class TicketHistoryServiceImpl(
    private val ticketHistoryRepository: TicketHistoryRepository,
    private val ticketService: TicketService,
    private val ticketRepository: TicketRepository
): TicketHistoryService {
    @Transactional(readOnly = true)
    override fun getAllTicketHistory(): List<TicketHistoryDTO> {
        return ticketHistoryRepository.findAll().map {it.toDTO()}
    }

    @Transactional(readOnly = true)
    override fun getTicketHistory(ticketId: Long): List<TicketHistoryDTO> {
        val ticket = getTicket(ticketId)
        return ticketHistoryRepository.findAllByTicket(ticket).map {it.toDTO()}
    }

    private fun getTicket(ticketId: Long): Ticket {
        val ticketDTO = ticketService.getTicket(ticketId)
        return ticketRepository.findByIdOrNull(ticketDTO.ticketId)!!
    }
}