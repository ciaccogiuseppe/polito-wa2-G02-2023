package it.polito.wa2.server.ticketing.message

import it.polito.wa2.server.BadRequestMessageException
import it.polito.wa2.server.UnprocessableMessageException
import it.polito.wa2.server.UnprocessableTicketException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@RestController
class MessageController(private val messageService: MessageService) {
    @GetMapping("/API/chat/{ticketId}")
    fun getMessage(@PathVariable ticketId: Long) : List<MessageDTO> {
        checkTicketId(ticketId)
        return messageService.getChat(ticketId)
    }

    @PostMapping("/API/chat/{ticketId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun addMessage(@PathVariable ticketId: Long, @RequestBody @Valid message: MessageDTO?, br: BindingResult) {
        checkTicketId(ticketId)
        checkInputMessage(message, br)
        messageService.addMessage(ticketId, message!!)
    }

    fun checkTicketId(ticketId: Long){
        if(ticketId <= 0)
            throw UnprocessableTicketException("Wrong ticket id value")
    }
    fun checkInputMessage(message: MessageDTO?, br: BindingResult){
        if (br.hasErrors())
            throw UnprocessableMessageException("Wrong message format")
        if (message == null)
            throw BadRequestMessageException("Message must not be NULL")
    }
}