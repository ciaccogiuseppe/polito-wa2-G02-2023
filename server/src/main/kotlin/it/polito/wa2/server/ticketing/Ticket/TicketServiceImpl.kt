package it.polito.wa2.server.ticketing.Ticket

import org.springframework.stereotype.Service

@Service
class TicketServiceImpl(
    private val ticketRepository: TicketRepository
): TicketService {}