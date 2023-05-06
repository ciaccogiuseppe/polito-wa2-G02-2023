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
    @GetMapping("/API/ticketing/{ticket_id}")
    fun getTicket(@PathVariable ticket_id: Long): TicketDTO {
        return ticketService.getTicket(ticket_id)
    }

    @GetMapping("/API/ticketing/filter")
    fun getTicketsFiltered(
        customer_id: Long?,
        min_priority: Int?,
        max_priority: Int?,
        product_id: String?,
        created_after: Timestamp?,
        created_before: Timestamp?,
        expert_id: Long?,
        status: List<String>?
    ): List<TicketDTO> {
        checkFilterParameters(
            customer_id, min_priority, max_priority, product_id,
            created_after, created_before, expert_id, status
        )
        return ticketService.getTicketsFiltered(
            customer_id, min_priority, max_priority, product_id,
            created_after, created_before, expert_id, status
        )
    }

    @PostMapping("/API/ticketing/")
    fun addTicket(@RequestBody @Valid ticket: TicketDTO?, br: BindingResult): Long {
        checkAddParameters(ticket, br)
        return ticketService.addTicket(ticket!!)
    }

    fun checkFilterParameters(
        customer_id: Long?,
        min_priority: Int?,
        max_priority: Int?,
        product_id: String?,
        created_after: Timestamp?,
        created_before: Timestamp?,
        expert_id: Long?,
        status: List<String>?
    ) {
        if ((min_priority != null) && (min_priority < 0))
            throw UnprocessableTicketException("Invalid min priority")
        if ((max_priority != null) && (max_priority < 0))
            throw UnprocessableTicketException("Invalid max priority")
        if (created_after != null && created_before != null && created_after.after(created_before))
            throw UnprocessableTicketException("<created_after> is after <created_before>")
        if (status != null && validStates.containsAll(status))
            throw UnprocessableTicketException("Some states are invalid")
    }

    fun checkAddParameters(ticket: TicketDTO?, br: BindingResult) {
        if (br.hasErrors())
            throw UnprocessableProfileException("Wrong ticket format")
        if (ticket == null)
            throw BadRequestProfileException("Ticket must not be NULL")
        if (ticket.productId == null)
            throw UnprocessableTicketException("Wrong ticket format")
    }
}