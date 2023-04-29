package it.polito.wa2.server.ticketing.TicketHistory


import org.springframework.stereotype.Service

@Service
class TicketHistoryServiceImpl(
    private val ticketHistoryRepository: TicketHistoryRepository
): TicketHistoryService {}