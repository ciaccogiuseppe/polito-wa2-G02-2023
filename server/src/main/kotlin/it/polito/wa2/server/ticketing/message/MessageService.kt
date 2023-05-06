package it.polito.wa2.server.ticketing.message

interface MessageService {
    fun getChat(ticketId: String): List<MessageDTO>

    fun addMessage(ticketId: String, message: MessageDTO)
}