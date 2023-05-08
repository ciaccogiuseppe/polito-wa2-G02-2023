package it.polito.wa2.server.ticketing.tickethistory


import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.ticketing.ticket.TicketRepository
import it.polito.wa2.server.ticketing.ticket.TicketService
import it.polito.wa2.server.ticketing.ticket.toTicket
import org.springframework.stereotype.Service

@Service
class TicketHistoryServiceImpl(
    private val ticketHistoryRepository: TicketHistoryRepository,
    private val ticketService: TicketService,
    private val ticketRepository: TicketRepository,
    private val productRepository: ProductRepository,
    private val profileRepository: ProfileRepository
): TicketHistoryService {
    override fun getAllTicketHistory(): List<TicketHistoryDTO> {
        return ticketHistoryRepository.findAll().map {it.toDTO()}
    }

    override fun getTicketHistory(ticketId: Long): List<TicketHistoryDTO> {
        val ticket = ticketService.getTicket(ticketId).toTicket(ticketRepository, productRepository, profileRepository)
        return ticketHistoryRepository.findAllByTicket(ticket).map {it.toDTO()}
    }

}