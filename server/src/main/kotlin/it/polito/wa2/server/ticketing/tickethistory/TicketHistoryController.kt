package it.polito.wa2.server.ticketing.tickethistory

import org.springframework.web.bind.annotation.RestController

@RestController
class TicketHistoryController(private val ticketHistoryService: TicketHistoryService) {
}