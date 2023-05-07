package it.polito.wa2.server.ticketing.tickethistory

import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.ticketing.ticket.Ticket
import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name="tickets_history")
class TicketHistory {
    @ManyToOne
    @JoinColumn(name="ticket_id")
    var ticket : Ticket? = null

    @ManyToOne
    var user : Profile? = null

    @ManyToOne
    var currentExpert : Profile? = null

    @Temporal(TemporalType.TIMESTAMP)
    var updatedTimestamp : Timestamp? = null

    var oldState : String = ""
    var newState : String = ""



    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ticket_history_generator")
    @SequenceGenerator(name = "ticket_history_generator",
        sequenceName = "tickets_history_id_seq",
        initialValue = 1,
        allocationSize = 1
    )
    var historyId : Long? = null
}