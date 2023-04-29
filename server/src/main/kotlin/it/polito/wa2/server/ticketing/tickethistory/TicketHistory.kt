package it.polito.wa2.server.ticketing.tickethistory

import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.ticketing.ticket.Ticket
import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name="ticket_history")
class TicketHistory {
    @ManyToOne
    var ticket_id : Ticket? = null

    @ManyToOne
    var user_id : Profile? = null

    @ManyToOne
    var current_expert_id : Profile? = null

    @Temporal(TemporalType.TIMESTAMP)
    var timestamp : Timestamp? = null

    var old_state : String = ""
    var new_state : String = ""



    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ticket_history_generator")
    @SequenceGenerator(name = "ticket_history_generator",
        sequenceName = "ticket_history_id_seq",
        initialValue = 1,
        allocationSize = 1
    )
    var history_id : Long? = null
}