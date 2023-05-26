package it.polito.wa2.server.ticketing.tickethistory

import it.polito.wa2.server.BadRequestFilterException
import it.polito.wa2.server.UnprocessableProfileException
import it.polito.wa2.server.UnprocessableTicketException
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.sql.Timestamp
import java.time.LocalDateTime

@RestController
class TicketHistoryController(private val ticketHistoryService: TicketHistoryService) {

    @GetMapping("/API/manager/ticketing/history/filter")
    fun getTicketHistoryFiltered(
        principal: Principal,
        @RequestParam(name="ticketId", required=false) ticketId: Long?,
        @RequestParam(name="userEmail", required=false) userEmail: String?,
        @RequestParam(name="updatedAfter", required=false) updatedAfter: LocalDateTime?,
        @RequestParam(name="updatedBefore", required=false) updatedBefore: LocalDateTime?,
        @RequestParam(name="currentExpertEmail", required=false) currentExpertEmail: String?
    ): List<TicketHistoryDTO> {
        val token: JwtAuthenticationToken = principal as JwtAuthenticationToken
        val loggedUserEmail = token.tokenAttributes["email"] as String
        checkFilterParameters(
            ticketId, userEmail,
            updatedAfter?.let{ Timestamp.valueOf(updatedAfter)}, updatedBefore?.let{ Timestamp.valueOf(updatedBefore)},
            currentExpertEmail
        )
        return ticketHistoryService.getTicketHistoryFiltered(
            ticketId, userEmail,
            updatedAfter?.let{ Timestamp.valueOf(updatedAfter)}, updatedBefore?.let{ Timestamp.valueOf(updatedBefore)},
            currentExpertEmail, loggedUserEmail
        )
    }

    fun checkFilterParameters(
        ticketId: Long?,
        userEmail: String?,
        updatedAfter: Timestamp?,
        updatedBefore: Timestamp?,
        currentExpertEmail: String?,
    ) {
        if(ticketId == null && userEmail == null && currentExpertEmail == null &&
            updatedAfter == null && updatedBefore == null)
            throw BadRequestFilterException("All filter parameters cannot be null")
        if (updatedAfter != null && updatedBefore != null && updatedAfter.after(updatedBefore))
            throw UnprocessableTicketException("<updated_after> is after <updated_before>")
        if (ticketId != null && ticketId < 0) {
            throw UnprocessableTicketException("Negative ticketId")
        }
        if (userEmail != null) {
            checkEmail(userEmail)
        }
        if (currentExpertEmail != null) {
            checkEmail(currentExpertEmail)
        }
    }

    fun checkEmail(email: String){
        if (!email.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")))
            throw UnprocessableProfileException("Wrong email format")
    }
}