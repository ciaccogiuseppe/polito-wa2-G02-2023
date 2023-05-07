package it.polito.wa2.server.ticketing.tickethistory

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class TicketHistoryController(private val ticketHistoryService: TicketHistoryService) {
    @GetMapping("/API/ticketing/history/")
    fun getAllTicketHistory(): List<TicketHistoryDTO> =
        ticketHistoryService.getAllTicketHistory()

    @GetMapping("/API/ticketing/history/{ticketId}")
    fun getTicketHistory(@PathVariable ticketId: Long): List<TicketHistoryDTO>{
        return ticketHistoryService.getTicketHistory(ticketId)
    }
}