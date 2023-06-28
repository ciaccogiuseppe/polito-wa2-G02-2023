package it.polito.wa2.server.ticketing.message

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.BadRequestMessageException
import it.polito.wa2.server.UnprocessableMessageException
import it.polito.wa2.server.UnprocessableTicketException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import java.security.Principal

@CrossOrigin(origins = ["http://localhost:3001"])
@RestController
@Observed
class MessageController(private val messageService: MessageService) {
    @GetMapping("/API/chat/{ticketId}")
    fun getMessage(principal: Principal, @PathVariable ticketId: Long): List<MessageDTO> {
        val userEmail = retrieveUserEmail(principal)
        checkTicketId(ticketId)
        return messageService.getChat(ticketId, userEmail)
    }

    @GetMapping("/API/manager/chat/{ticketId}")
    fun getMessageManager(principal: Principal, @PathVariable ticketId: Long): List<MessageDTO> {
        val userEmail = retrieveUserEmail(principal)
        checkTicketId(ticketId)
        return messageService.getChatManager(ticketId, userEmail)
    }

    @PostMapping("/API/chat/{ticketId}")
    @ResponseStatus(HttpStatus.CREATED)
    fun addMessage(
        principal: Principal,
        @PathVariable ticketId: Long,
        @RequestBody @Valid messageDTO: MessageDTO?,
        br: BindingResult
    ) {
        val userEmail = retrieveUserEmail(principal)
        checkTicketId(ticketId)
        checkInputMessage(messageDTO, br)
        messageService.addMessageSender(ticketId, messageDTO!!, userEmail)
    }


    private fun checkTicketId(ticketId: Long) {
        if (ticketId <= 0)
            throw UnprocessableTicketException("Wrong ticket id value")
    }

    private fun checkInputMessage(messageDTO: MessageDTO?, br: BindingResult) {
        if (br.hasErrors())
            throw UnprocessableMessageException("Wrong message format")
        if (messageDTO == null)
            throw BadRequestMessageException("Message must not be NULL")
    }

    private fun retrieveUserEmail(principal: Principal): String {
        val token: JwtAuthenticationToken = principal as JwtAuthenticationToken
        return token.tokenAttributes["email"] as String
    }
}