package it.polito.wa2.server.ticketing.message

import org.springframework.web.bind.annotation.RestController

@RestController
class MessageController(private val messageService: MessageService) {
}