package it.polito.wa2.server.ticketing.tickethistory

import java.sql.Timestamp

interface TicketHistoryService {
    fun getAllTicketHistory(): List<TicketHistoryDTO>

    fun getTicketHistoryFiltered(
        ticketId: Long?,
        userId: Long?,
        updatedAfter: Timestamp?,
        updatedBefore: Timestamp?,
        currentExpertId: Long?,): List<TicketHistoryDTO>
}