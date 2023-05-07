package it.polito.wa2.server.ticketing.ticket

import it.polito.wa2.server.products.ProductDTO
import it.polito.wa2.server.products.toDTO
import it.polito.wa2.server.products.toProduct
import it.polito.wa2.server.profiles.ProfileDTO
import it.polito.wa2.server.profiles.toDTO
import it.polito.wa2.server.profiles.toProfile
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
    val product: ProductDTO?,
    val customer: ProfileDTO?,
    val expert: ProfileDTO?,
    val status: String,
    val createdTimestamp: Timestamp?
)

fun Ticket.toDTO(): TicketDTO {
    return TicketDTO(
        ticketId,
        title,
        description,
        priority,
        product?.toDTO(),
        customer?.toDTO(),
        expert?.toDTO(),
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
    ticket.product = product?.toProduct()
    ticket.customer = customer?.toProfile()
    ticket.expert = expert?.toProfile()
    ticket.status = status
    ticket.createdTimestamp = createdTimestamp
    return ticket
}