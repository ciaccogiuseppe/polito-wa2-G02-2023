package it.polito.wa2.server.ticketing.ticket

import it.polito.wa2.server.products.Product
import it.polito.wa2.server.profiles.Profile
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.sql.Timestamp

data class TicketDTO(
    val ticketId : Long?,
    @field:NotBlank(message = "A title is required")
    val title: String,
    @field:NotBlank
    val description: String,
    @field:NotBlank
    val priority: Int,
    @field:Size(min = 13, max = 13)
    val product: Product?,
    val customer: Profile?,
    val expert: Profile?,
    val status: String,
    val createdTimestamp: Timestamp?
)

fun Ticket.toDTO(): TicketDTO {
    return TicketDTO(
        ticketId,
        title,
        description,
        priority,
        product,
        customer,
        expert,
        status,
        createdTimestamp
    )
}

fun TicketDTO.toTicket(): Ticket {
    val ticket = Ticket()
    ticket.ticketId = ticketId
    ticket.title = title
    ticket.description = description
    ticket.priority = priority
    ticket.product = product
    ticket.customer = customer
    ticket.expert = expert
    ticket.status = status
    ticket.createdTimestamp = createdTimestamp
    return ticket
}