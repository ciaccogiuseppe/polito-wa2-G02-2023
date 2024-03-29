package it.polito.wa2.server.ticketing.message

interface MessageService {
    fun getChat(ticketId: Long, userEmail: String): List<MessageDTO>

    fun getChatManager(ticketId: Long, userEmail: String): List<MessageDTO>

    fun addMessageSender(ticketId: Long, messageDTO: MessageDTO, userEmail: String)

    fun addMessageManager(ticketId: Long, messageDTO: MessageDTO, userEmail: String)
}