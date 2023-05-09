package it.polito.wa2.server.ticketing.ticket

import it.polito.wa2.server.products.ProductDTO
import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.products.toDTO
//import it.polito.wa2.server.products.toProduct
import it.polito.wa2.server.profiles.ProfileDTO
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.toDTO
import jakarta.validation.constraints.*
// import it.polito.wa2.server.profiles.toProfile
import org.springframework.data.repository.findByIdOrNull
import java.sql.Timestamp

data class TicketDTO(
    @field:Positive
    val ticketId : Long?,
    @field:NotBlank(message = "A title is required")
    val title: String,
    @field:NotBlank
    val description: String,
    @field:PositiveOrZero
    val priority: Int,
    @field:Size(min = 13, max = 13)
    val productId: String,
    @field:Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$",
        message="email must be valid")
    val customerId: String?,
    @field:Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$",
        message="email must be valid")
    val expertId: String?,
    val status: TicketStatus?,
    val createdTimestamp: Timestamp?
)

fun Ticket.toDTO(): TicketDTO {
    return TicketDTO(
        ticketId,
        title,
        description,
        priority,
        product?.productId!!,
        customer?.email,
        expert?.email,
        status,
        createdTimestamp
    )
}
/*
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
*/
