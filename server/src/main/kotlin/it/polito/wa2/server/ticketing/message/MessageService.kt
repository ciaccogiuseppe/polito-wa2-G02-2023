package it.polito.wa2.server.ticketing.message

interface MessageService {
    fun getChat(ticketId: Long, userEmail: String): List<MessageDTO>

    fun addMessage(ticketId: Long, messageDTO: MessageDTO, userEmail: String)
}