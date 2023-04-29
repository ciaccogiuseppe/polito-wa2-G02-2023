package it.polito.wa2.server.ticketing.ticket

import org.springframework.web.bind.annotation.*

@RestController
class TicketController(private val ticketService: TicketService) {
}