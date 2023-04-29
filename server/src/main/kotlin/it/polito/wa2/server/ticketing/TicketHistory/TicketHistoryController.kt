package it.polito.wa2.server.ticketing.TicketHistory

import org.springframework.web.bind.annotation.RestController

@RestController
class TicketHistoryController(private val ticketHistoryService: TicketHistoryService) {
}