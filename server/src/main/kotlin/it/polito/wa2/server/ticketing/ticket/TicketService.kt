package it.polito.wa2.server.ticketing.ticket

import java.sql.Timestamp


interface TicketService {
    fun getTicket(ticket_id: Long): TicketDTO

    fun getTicketsFiltered(
        customer_id: Long?,
        min_priority: Int?,
        max_priority: Int?,
        product_id: String?,
        created_after: Timestamp?,
        created_before: Timestamp?,
        expert_id: Long?,
        status: List<String>?
    ): List<TicketDTO>

    fun addTicket(ticket: TicketDTO): Long

}