package it.polito.wa2.server.ticketing.ticket

import it.polito.wa2.server.BadRequestProfileException
import it.polito.wa2.server.UnprocessableProfileException
import it.polito.wa2.server.UnprocessableTicketException
import jakarta.validation.Valid
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import java.sql.Timestamp

@RestController
class TicketController(private val ticketService: TicketService) {
    private val validStates = arrayListOf("OPEN", "RESOLVED", "CLOSED", "IN PROGRESS", "REOPENED")
    @GetMapping("/API/ticketing/{ticketId}")
    fun getTicket(@PathVariable ticketId: Long): TicketDTO {
        return ticketService.getTicket(ticketId)
    }

    @GetMapping("/API/ticketing/filter")
    fun getTicketsFiltered(
        customerId: Long?,
        minPriority: Int?,
        maxPriority: Int?,
        productId: String?,
        createdAfter: Timestamp?,
        createdBefore: Timestamp?,
        expertId: Long?,
        status: List<String>?
    ): List<TicketDTO> {
        checkFilterParameters(
            customerId, minPriority, maxPriority, productId,
            createdAfter, createdBefore, expertId, status
        )
        return ticketService.getTicketsFiltered(
            customerId, minPriority, maxPriority, productId,
            createdAfter, createdBefore, expertId, status
        )
    }

    @PostMapping("/API/ticketing/")
    fun addTicket(@RequestBody @Valid ticket: TicketDTO?, br: BindingResult): Long {
        checkAddParameters(ticket, br)
        return ticketService.addTicket(ticket!!)
    }

    fun checkFilterParameters(
        customerId: Long?,
        minPriority: Int?,
        maxPriority: Int?,
        productId: String?,
        createdAfter: Timestamp?,
        createdBefore: Timestamp?,
        expertId: Long?,
        status: List<String>?
    ) {
        if ((minPriority != null) && (minPriority < 0))
            throw UnprocessableTicketException("Invalid min priority")
        if ((maxPriority != null) && (maxPriority < 0))
            throw UnprocessableTicketException("Invalid max priority")
        if (createdAfter != null && createdBefore != null && createdAfter.after(createdBefore))
            throw UnprocessableTicketException("<created_after> is after <created_before>")
        if (status != null && validStates.containsAll(status))
            throw UnprocessableTicketException("Some states are invalid")
    }

    fun checkAddParameters(ticket: TicketDTO?, br: BindingResult) {
        if (br.hasErrors())
            throw UnprocessableProfileException("Wrong ticket format")
        if (ticket == null)
            throw BadRequestProfileException("Ticket must not be NULL")
        if (ticket.product == null)
            throw UnprocessableTicketException("Wrong ticket format")
    }
}