package it.polito.wa2.server.ticketing.Message


import org.springframework.stereotype.Service

@Service
class MessageServiceImpl(
    private val messageRepository: MessageRepository
): MessageService {}