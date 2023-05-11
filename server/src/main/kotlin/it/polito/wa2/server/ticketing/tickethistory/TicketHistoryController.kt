package it.polito.wa2.server.ticketing.tickethistory

import it.polito.wa2.server.BadRequestFilterException
import it.polito.wa2.server.UnprocessableTicketException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.sql.Timestamp
import java.time.LocalDateTime

@RestController
class TicketHistoryController(private val ticketHistoryService: TicketHistoryService) {

    @GetMapping("/API/ticketing/history/filter")
    fun getTicketHistoryFiltered(
        @RequestParam(name="ticketId", required=false) ticketId: Long?,
        @RequestParam(name="userId", required=false) userId: Long?,
        @RequestParam(name="updatedAfter", required=false) updatedAfter: LocalDateTime?,
        @RequestParam(name="updatedBefore", required=false) updatedBefore: LocalDateTime?,
        @RequestParam(name="currentExpertId", required=false) currentExpertId: Long?
    ): List<TicketHistoryDTO> {
        checkFilterParameters(
            ticketId, userId,
            updatedAfter?.let{ Timestamp.valueOf(updatedAfter)}, updatedBefore?.let{ Timestamp.valueOf(updatedBefore)},
            currentExpertId
        )
        return ticketHistoryService.getTicketHistoryFiltered(
            ticketId, userId,
            updatedAfter?.let{ Timestamp.valueOf(updatedAfter)}, updatedBefore?.let{ Timestamp.valueOf(updatedBefore)}, currentExpertId
        )
    }

    fun checkFilterParameters(
        ticketId: Long?,
        userId: Long?,
        updatedAfter: Timestamp?,
        updatedBefore: Timestamp?,
        currentExpertId: Long?,
    ) {
        if(ticketId == null && userId == null && currentExpertId == null &&
            updatedAfter == null && updatedBefore == null)
            throw BadRequestFilterException("All filter parameters cannot be null")
        if (updatedAfter != null && updatedBefore != null && updatedAfter.after(updatedBefore))
            throw UnprocessableTicketException("<updated_after> is after <updated_before>")
    }
}