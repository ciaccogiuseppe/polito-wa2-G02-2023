package it.polito.wa2.server.ticketing.TicketHistory

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface TicketHistoryRepository: JpaRepository<TicketHistory, String> {
}