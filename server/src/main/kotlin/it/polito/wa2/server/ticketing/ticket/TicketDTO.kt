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
    val productId: Product?,
    val customerId: Profile?,
    val expertId: Profile?,
    val status: String,
    val createdDate: Timestamp?
)

fun Ticket.toDTO(): TicketDTO {
    return TicketDTO(
        ticketId,
        title,
        description,
        priority,
        product_id,
        customer_id,
        expert_id,
        status,
        timestamp
    )
}

fun TicketDTO.toTicket(): Ticket {
    val ticket = Ticket()
    ticket.ticketId = ticketId
    ticket.title = title
    ticket.description = description
    ticket.priority = priority
    ticket.product_id = productId
    ticket.customer_id = customerId
    ticket.expert_id = expertId
    ticket.status = status
    ticket.timestamp = createdDate
    return ticket
}