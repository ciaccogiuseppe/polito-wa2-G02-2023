package it.polito.wa2.server.ticketing.message

interface MessageService {
    fun getChat(ticketId: Long): List<MessageDTO>

    fun addMessage(ticketId: Long, messageDTO: MessageDTO)
}