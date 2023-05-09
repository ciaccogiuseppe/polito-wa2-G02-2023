package it.polito.wa2.server.ticketing.tickethistory

import it.polito.wa2.server.profiles.ProfileDTO
import it.polito.wa2.server.profiles.toDTO
import it.polito.wa2.server.ticketing.ticket.TicketDTO
import it.polito.wa2.server.ticketing.ticket.TicketStatus
import it.polito.wa2.server.ticketing.ticket.toDTO
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import java.sql.Timestamp

data class TicketHistoryDTO(
    @field:Positive
    val historyId : Long?,
    @field:Positive
    val ticketId: Long,
    @field:NotBlank(message="email is mandatory")
    @field:Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$",
        message="email must be valid")
    val user: String,
    @field:Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$",
        message="email must be valid")
    val currentExpert: String?,
    val updatedTimestamp: Timestamp,
    val oldState: TicketStatus,
    val newState: TicketStatus
)

fun TicketHistory.toDTO(): TicketHistoryDTO {
    return TicketHistoryDTO(historyId!!, ticket?.ticketId!!,
        user?.email!!, currentExpert?.email!!, updatedTimestamp!!,
        oldState, newState)
}