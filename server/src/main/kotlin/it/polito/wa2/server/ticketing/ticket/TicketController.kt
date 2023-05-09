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
    // private val validStates = arrayListOf("OPEN", "RESOLVED", "CLOSED", "IN PROGRESS", "REOPENED")
    @GetMapping("/API/ticketing/{ticketId}")
    fun getTicket(@PathVariable ticketId: Long): TicketDTO {
        if(ticketId < 1)
            throw UnprocessableTicketException("Invalid ticket id")
        return ticketService.getTicket(ticketId)
    }

    @GetMapping("/API/ticketing/filter")
    fun getTicketsFiltered(
        @RequestParam(name="customerId", required=false) customerId: Long?,
        @RequestParam(name="minPriority", required=false) minPriority: Int?,
        @RequestParam(name="maxPriority", required=false) maxPriority: Int?,
        @RequestParam(name="productId", required=false) productId: String?,
        @RequestParam(name="createdAfter", required=false) createdAfter: Timestamp?,
        @RequestParam(name="createdBefore", required=false) createdBefore: Timestamp?,
        @RequestParam(name="expertId", required=false) expertId: Long?,
        @RequestParam(name="status", required=false) status: List<TicketStatus>?
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
    fun addTicket(@RequestBody @Valid ticket: TicketDTO?, br: BindingResult): TicketIdDTO {
        checkAddParameters(ticket, br)
        return ticketService.addTicket(ticket!!)
    }

    @PutMapping("/API/ticketing/assign")
    fun assignTicket(){
        TODO("Not yet implemented")
    }

    @PutMapping("/API/ticketing/update")
    fun updateTicket(){
        TODO("Not yet implemented")
    }

    fun checkFilterParameters(
        customerId: Long?,
        minPriority: Int?,
        maxPriority: Int?,
        productId: String?,
        createdAfter: Timestamp?,
        createdBefore: Timestamp?,
        expertId: Long?,
        status: List<TicketStatus>?
    ) {
        if ((minPriority != null) && (minPriority < 0))
            throw UnprocessableTicketException("Invalid min priority")
        if ((maxPriority != null) && (maxPriority < 0))
            throw UnprocessableTicketException("Invalid max priority")
        if (createdAfter != null && createdBefore != null && createdAfter.after(createdBefore))
            throw UnprocessableTicketException("<created_after> is after <created_before>")
    }

    fun checkAddParameters(ticket: TicketDTO?, br: BindingResult) {
        if (br.hasErrors())
            throw UnprocessableProfileException("Wrong ticket format")
        if (ticket == null)
            throw BadRequestProfileException("Ticket must not be NULL")
    }
}