package it.polito.wa2.server.ticketing.ticket

import it.polito.wa2.server.products.ProductDTO
import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.products.toDTO
import it.polito.wa2.server.products.toProduct
import it.polito.wa2.server.profiles.ProfileDTO
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.toDTO
import it.polito.wa2.server.profiles.toProfile
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import org.springframework.data.repository.findByIdOrNull
import java.sql.Timestamp

data class TicketDTO(
    val ticketId : Long?,
    @field:NotBlank(message = "A title is required")
    val title: String,
    @field:NotBlank
    val description: String,
    @field:PositiveOrZero
    val priority: Int,
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

fun TicketDTO.toTicket(
    ticketRepository: TicketRepository,
    productRepository: ProductRepository,
    profileRepository: ProfileRepository): Ticket {
    var ticket = if(ticketId!=null){ticketRepository.findByIdOrNull(ticketId)} else {null}
    if(ticket != null)
        return ticket
    ticket = Ticket()
    ticket.ticketId = ticketId
    ticket.title = title
    ticket.description = description
    ticket.priority = priority
    ticket.product = product?.toProduct(productRepository)
    ticket.customer = customer?.toProfile(profileRepository)
    ticket.expert = expert?.toProfile(profileRepository)
    ticket.status = status
    ticket.createdTimestamp = createdTimestamp
    return ticket
}