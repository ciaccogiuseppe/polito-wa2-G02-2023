package it.polito.wa2.server.ticketing.tickethistory

import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.ticketing.ticket.Ticket
import it.polito.wa2.server.ticketing.ticket.TicketStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.sql.Timestamp
import java.time.LocalDateTime

data class TicketHistoryDTO(
    @field:Positive
    val ticketId: Long,
    @field:NotBlank(message="email is mandatory")
    @field:Positive
    val userEmail: String,
    @field:Positive
    val currentExpertEmail: String?,
    val updatedTimestamp: Timestamp?,
    val oldState: TicketStatus,
    val newState: TicketStatus,
    @field:Positive
    val historyId: Long?
)

fun TicketHistory.toDTO(): TicketHistoryDTO {
    return TicketHistoryDTO(ticket?.getId()!!,
        user?.email!!, currentExpert?.email, updatedTimestamp,
        oldState, newState, this.getId())
}

fun newTicketHistory(
    ticket: Ticket,
    user: Profile,
    currentExpert: Profile?,
    oldState: TicketStatus,
    newState: TicketStatus
): TicketHistory {
    val ticketHistory = TicketHistory()
    ticketHistory.ticket = ticket
    ticketHistory.user = user
    ticketHistory.currentExpert = currentExpert
    ticketHistory.updatedTimestamp = Timestamp.valueOf(LocalDateTime.now())
    ticketHistory.oldState = oldState
    ticketHistory.newState = newState
    return ticketHistory
}