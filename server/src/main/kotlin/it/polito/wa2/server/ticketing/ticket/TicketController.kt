package it.polito.wa2.server.ticketing.ticket

import it.polito.wa2.server.BadRequestFilterException
import it.polito.wa2.server.BadRequestProfileException
import it.polito.wa2.server.UnprocessableProfileException
import it.polito.wa2.server.UnprocessableTicketException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.sql.Timestamp
import java.time.LocalDateTime

@RestController
class TicketController(private val ticketService: TicketService) {
    @GetMapping("/API/ticketing/{ticketId}")
    fun getTicket(principal: Principal, @PathVariable ticketId: Long): TicketDTO {
        val token: JwtAuthenticationToken = principal as JwtAuthenticationToken
        val userEmail = token.tokenAttributes["email"] as String
        if(ticketId < 1)
            throw UnprocessableTicketException("Invalid ticket id")
        return ticketService.getTicket(ticketId, userEmail)
    }

    @GetMapping("/API/ticketing/filter")
    fun getTicketsFiltered(
        principal: Principal,
        @RequestParam(name="customerEmail", required=false) customerEmail: String?,
        @RequestParam(name="minPriority", required=false) minPriority: Int?,
        @RequestParam(name="maxPriority", required=false) maxPriority: Int?,
        @RequestParam(name="productId", required=false) productId: String?,
        @RequestParam(name="createdAfter", required=false) createdAfter: LocalDateTime?,
        @RequestParam(name="createdBefore", required=false) createdBefore: LocalDateTime?,
        @RequestParam(name="expertEmail", required=false) expertEmail: String?,
        @RequestParam(name="status", required=false) status: List<TicketStatus>?
    ): List<TicketDTO> {
        val token: JwtAuthenticationToken = principal as JwtAuthenticationToken
        val userEmail = token.tokenAttributes["email"] as String
        checkFilterParameters(
            customerEmail, minPriority, maxPriority, productId,
            createdAfter?.let{Timestamp.valueOf(createdAfter)}, createdBefore?.let{Timestamp.valueOf(createdBefore)}, expertEmail, status
        )
        return ticketService.getTicketsFiltered(
            customerEmail, minPriority, maxPriority, productId,
            createdAfter?.let{Timestamp.valueOf(createdAfter)}, createdBefore?.let{Timestamp.valueOf(createdBefore)}, expertEmail,
            status, userEmail
        )
    }

    @PostMapping("/API/client/ticketing/")
    @ResponseStatus(HttpStatus.CREATED)
    fun addTicket(principal: Principal, @RequestBody @Valid ticketDTO: TicketDTO?, br: BindingResult): TicketIdDTO {
        val token: JwtAuthenticationToken = principal as JwtAuthenticationToken
        val userEmail = token.tokenAttributes["email"] as String
        checkAddParameters(ticketDTO, br)
        return ticketService.addTicket(ticketDTO!!, userEmail)
    }

    @PutMapping("/API/manager/ticketing/assign")
    fun assignTicket(principal: Principal, @RequestBody @Valid ticketAssignDTO: TicketAssignDTO?, br: BindingResult){
        val token: JwtAuthenticationToken = principal as JwtAuthenticationToken
        val userEmail = token.tokenAttributes["email"] as String
        checkAssignParameters(ticketAssignDTO, br)
        ticketService.assignTicket(ticketAssignDTO!!, userEmail)
    }

    @PutMapping("/API/manager/ticketing/update")
    fun managerUpdateTicket(principal: Principal, @RequestBody @Valid ticketUpdateDTO: TicketUpdateDTO?, br: BindingResult){
        val token: JwtAuthenticationToken = principal as JwtAuthenticationToken
        val userEmail = token.tokenAttributes["email"] as String
        checkUpdateParameters(ticketUpdateDTO, br)
        ticketService.managerUpdateTicket(ticketUpdateDTO!!, userEmail)
    }

    @PutMapping("/API/client/ticketing/update")
    fun clientUpdateTicket(principal: Principal, @RequestBody @Valid ticketUpdateDTO: TicketUpdateDTO?, br: BindingResult){
        val token: JwtAuthenticationToken = principal as JwtAuthenticationToken
        val userEmail = token.tokenAttributes["email"] as String
        checkUpdateParameters(ticketUpdateDTO, br)
        ticketService.clientUpdateTicket(ticketUpdateDTO!!, userEmail)
    }

    @PutMapping("/API/expert/ticketing/update")
    fun expertUpdateTicket(principal: Principal, @RequestBody @Valid ticketUpdateDTO: TicketUpdateDTO?, br: BindingResult){
        val token: JwtAuthenticationToken = principal as JwtAuthenticationToken
        val userEmail = token.tokenAttributes["email"] as String
        checkUpdateParameters(ticketUpdateDTO, br)
        ticketService.expertUpdateTicket(ticketUpdateDTO!!, userEmail)
    }

    fun checkFilterParameters(
        customerEmail: String?,
        minPriority: Int?,
        maxPriority: Int?,
        productId: String?,
        createdAfter: Timestamp?,
        createdBefore: Timestamp?,
        expertEmail: String?,
        status: List<TicketStatus>?
    ) {
        if(customerEmail == null && minPriority == null && maxPriority == null &&
            productId == null && createdAfter == null && createdBefore == null &&
            expertEmail == null && status == null)
            throw BadRequestFilterException("All filter parameters cannot be null")
        if ((minPriority != null) && (minPriority < 0))
            throw UnprocessableTicketException("Invalid min priority")
        if ((maxPriority != null) && (maxPriority < 0))
            throw UnprocessableTicketException("Invalid max priority")
        if (createdAfter != null && createdBefore != null && createdAfter.after(createdBefore))
            throw UnprocessableTicketException("<created_after> is after <created_before>")
    }

    fun checkAddParameters(ticketDTO: TicketDTO?, br: BindingResult) {
        if (br.hasErrors())
            throw UnprocessableProfileException("Wrong ticket format")
        if (ticketDTO == null)
            throw BadRequestProfileException("Ticket must not be NULL")
    }

    fun checkAssignParameters(ticketAssignDTO: TicketAssignDTO?, br: BindingResult) {
        if (br.hasErrors())
            throw UnprocessableProfileException("Wrong ticket format")
        if (ticketAssignDTO == null)
            throw BadRequestProfileException("Ticket must not be NULL")
    }

    fun checkUpdateParameters(ticketUpdateDTO: TicketUpdateDTO?, br: BindingResult) {
        if (br.hasErrors())
            throw UnprocessableProfileException("Wrong ticket format")
        if (ticketUpdateDTO == null)
            throw BadRequestProfileException("Ticket must not be NULL")
    }
}