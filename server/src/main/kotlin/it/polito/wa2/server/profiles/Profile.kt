package it.polito.wa2.server.profiles

import it.polito.wa2.server.ticketing.Message.Message
import it.polito.wa2.server.ticketing.Ticket.Ticket
import it.polito.wa2.server.ticketing.TicketHistory.TicketHistory
import jakarta.persistence.*

@Entity
@Table(name="profiles")
class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "profile_generator")
    @SequenceGenerator(name = "profile_generator",
        sequenceName = "profiles_profile_id_seq",
        initialValue = 1,
        allocationSize = 1
    )
    var profileId : Long? = null
    var email: String = ""
    var name: String = ""
    var surname: String = ""

    @OneToMany(mappedBy = "customer_id")
    val tickets_customer = mutableSetOf<Ticket>()

    @OneToMany(mappedBy = "expert_id")
    val tickets_expert = mutableSetOf<Ticket>()

    @OneToMany(mappedBy = "sender_id")
    val message_sender = mutableSetOf<Message>()

    @OneToMany(mappedBy = "user_id")
    val history_editor = mutableSetOf<TicketHistory>()

    @OneToMany(mappedBy = "current_expert_id")
    val history_expert = mutableSetOf<TicketHistory>()
}