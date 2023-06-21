package it.polito.wa2.server.ticketing.ticket

import it.polito.wa2.server.items.Item
import it.polito.wa2.server.profiles.Profile
import jakarta.validation.constraints.*
import java.sql.Timestamp

data class TicketDTO(
    @field:Positive
    val ticketId : Long?,
    @field:NotBlank(message = "A title is required")
    val title: String,
    @field:NotBlank
    val description: String,
    @field:PositiveOrZero
    val priority: Int?,
    @field:Size(min = 13, max = 13)
    val productId: String,
    @field:Positive
    val serialNum: Long,

    @field:Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$",
        message="email must be valid")
    val clientEmail: String?,
    @field:Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$",
        message="email must be valid")
    val expertEmail: String?,
    val status: TicketStatus?,
    val createdTimestamp: Timestamp?
)

data class TicketAssignDTO(
    @field:Positive
    val ticketId : Long,
    @field:Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$",
        message="email must be valid")
    val expertEmail: String,
    @field:PositiveOrZero
    val priority: Int
)

data class TicketUpdateDTO(
    @field:Positive
    val ticketId : Long,
    val newState: TicketStatus
)

fun Ticket.toDTO(): TicketDTO {
    return TicketDTO(
        this.getId(),
        title,
        description,
        priority,
        item?.product?.productId!!,
        item?.serialNum!!,
        client?.email,
        expert?.email,
        status,
        createdTimestamp
    )
}

fun TicketDTO.toNewTicket(
    item: Item,
    client: Profile,
    timestamp: Timestamp
): Ticket {
    val ticket = Ticket()
    ticket.title = title
    ticket.description = description
    ticket.priority = 0
    ticket.item = item
    ticket.client = client
    ticket.expert = null
    ticket.status = TicketStatus.OPEN
    ticket.createdTimestamp = timestamp
    return ticket
}

data class TicketIdDTO(val ticketId: Long)