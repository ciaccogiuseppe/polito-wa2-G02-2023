package it.polito.wa2.server.ticketing.tickethistory

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface TicketHistoryRepository: JpaRepository<TicketHistory, String> {
}