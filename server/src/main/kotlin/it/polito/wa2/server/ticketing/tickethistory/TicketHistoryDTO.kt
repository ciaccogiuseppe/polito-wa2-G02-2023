package it.polito.wa2.server.ticketing.tickethistory

import it.polito.wa2.server.profiles.ProfileDTO
import it.polito.wa2.server.profiles.toDTO
import it.polito.wa2.server.ticketing.ticket.TicketDTO
import it.polito.wa2.server.ticketing.ticket.toDTO
import java.sql.Timestamp

data class TicketHistoryDTO(
    val historyId : Long,
    val ticket: TicketDTO,
    val user: ProfileDTO,
    val currentExpert: ProfileDTO?,
    val updatedTimestamp: Timestamp,
    val oldState: String,
    val newState: String
)

fun TicketHistory.toDTO(): TicketHistoryDTO {
    val ticket: TicketDTO? = ticket?.toDTO()
    val user: ProfileDTO? = user?.toDTO()
    val currentExpert: ProfileDTO? = currentExpert?.toDTO()
    return TicketHistoryDTO(historyId!!, ticket!!,
        user!!, currentExpert!!, updatedTimestamp!!,
        oldState, newState)
}