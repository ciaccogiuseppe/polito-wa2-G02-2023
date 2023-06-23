package it.polito.wa2.server.ticketing.tickethistory

import it.polito.wa2.server.EntityBase
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.ticketing.ticket.Ticket
import it.polito.wa2.server.ticketing.ticket.TicketStatus
import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name = "tickets_history")
class TicketHistory : EntityBase<Long>() {
    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    var ticket: Ticket? = null

    @ManyToOne
    @JoinColumn(nullable = false)
    var user: Profile? = null

    @ManyToOne
    var currentExpert: Profile? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    var updatedTimestamp: Timestamp? = null

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    var oldState: TicketStatus = TicketStatus.OPEN

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    var newState: TicketStatus = TicketStatus.OPEN


}