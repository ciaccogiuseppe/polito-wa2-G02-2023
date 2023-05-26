package it.polito.wa2.server.ticketing.tickethistory

import java.sql.Timestamp

interface TicketHistoryService {
    fun getAllTicketHistory(): List<TicketHistoryDTO>

    fun getTicketHistoryFiltered(
        ticketId: Long?,
        userEmail: String?,
        updatedAfter: Timestamp?,
        updatedBefore: Timestamp?,
        currentExpertEmail: String?,
        loggedUserEmail: String
    ): List<TicketHistoryDTO>
}