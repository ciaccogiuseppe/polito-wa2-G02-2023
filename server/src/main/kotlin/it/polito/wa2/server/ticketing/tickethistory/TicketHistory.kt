package it.polito.wa2.server.ticketing.tickethistory

import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.ticketing.ticket.Ticket
import it.polito.wa2.server.ticketing.ticket.TicketStatus
import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name="tickets_history")
class TicketHistory {
    @ManyToOne
    @JoinColumn(name="ticket_id")
    @Column(nullable = false)
    var ticket : Ticket? = null

    @ManyToOne
    @Column(nullable = false)
    var user : Profile? = null

    @ManyToOne
    var currentExpert : Profile? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    var updatedTimestamp : Timestamp? = null

    @Enumerated(value=EnumType.STRING)
    @Column(nullable = false)
    var oldState : TicketStatus = TicketStatus.OPEN
    @Enumerated(value=EnumType.STRING)
    @Column(nullable = false)
    var newState :  TicketStatus =TicketStatus.OPEN



    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ticket_history_generator")
    @SequenceGenerator(name = "ticket_history_generator",
        sequenceName = "tickets_history_id_seq",
        initialValue = 1,
        allocationSize = 1
    )
    @Column(updatable = false, nullable = false)
    var historyId : Long? = null
}