package it.polito.wa2.server.ticketing.ticket

import java.sql.Timestamp


interface TicketService {
    fun getTicket(ticketId: Long): TicketDTO

    /*
    fun getTicketsFiltered(
        customerId: Long?,
        minPriority: Int?,
        maxPriority: Int?,
        productId: String?,
        createdAfter: Timestamp?,
        createdBefore: Timestamp?,
        expertId: Long?,
        status: List<String>?
    ): List<TicketDTO>
     */

    fun addTicket(ticket: TicketDTO): TicketIdDTO

}