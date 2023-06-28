package it.polito.wa2.server.ticketing.ticket

import java.sql.Timestamp


interface TicketService {
    fun managerGetTicket(ticketId: Long, managerEmail: String): TicketDTO
    fun clientGetTicket(ticketId: Long, clientEmail: String): TicketDTO
    fun expertGetTicket(ticketId: Long, expertEmail: String): TicketDTO

    fun clientGetTicketsFiltered(
        clientEmail: String?,
        minPriority: Int?,
        maxPriority: Int?,
        productId: String?,
        createdAfter: Timestamp?,
        createdBefore: Timestamp?,
        expertEmail: String?,
        status: List<TicketStatus>?,
        userEmail: String
    ): List<TicketDTO>

    fun expertGetTicketsFiltered(
        clientEmail: String?,
        minPriority: Int?,
        maxPriority: Int?,
        productId: String?,
        createdAfter: Timestamp?,
        createdBefore: Timestamp?,
        expertEmail: String?,
        status: List<TicketStatus>?,
        userEmail: String
    ): List<TicketDTO>

    fun managerGetTicketsFiltered(
        clientEmail: String?,
        minPriority: Int?,
        maxPriority: Int?,
        productId: String?,
        createdAfter: Timestamp?,
        createdBefore: Timestamp?,
        expertEmail: String?,
        status: List<TicketStatus>?,
        userEmail: String
    ): List<TicketDTO>

    fun addTicket(ticketDTO: TicketDTO, userEmail: String): TicketIdDTO
    fun assignTicket(ticketAssignDTO: TicketAssignDTO, userEmail: String)
    fun managerUpdateTicket(ticketUpdateDTO: TicketUpdateDTO, userEmail: String)
    fun clientUpdateTicket(ticketUpdateDTO: TicketUpdateDTO, userEmail: String)
    fun expertUpdateTicket(ticketUpdateDTO: TicketUpdateDTO, userEmail: String)

}