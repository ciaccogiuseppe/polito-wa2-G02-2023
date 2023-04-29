package it.polito.wa2.server.ticketing.Ticket

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@RestController
class TicketController(private val ticketService: TicketService) {
}