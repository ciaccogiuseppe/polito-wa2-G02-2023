package it.polito.wa2.server.ticketing.tickethistory

interface TicketHistoryService {
    fun getAllTicketHistory(): List<TicketHistoryDTO>

    fun getTicketHistory(ticketId: Long): List<TicketHistoryDTO>

    fun addTicketHistory(ticketHistoryDTO: TicketHistoryDTO)
}