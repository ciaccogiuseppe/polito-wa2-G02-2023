package it.polito.wa2.server.ticketing.tickethistory


import org.springframework.stereotype.Service

@Service
class TicketHistoryServiceImpl(
    private val ticketHistoryRepository: TicketHistoryRepository
): TicketHistoryService {}