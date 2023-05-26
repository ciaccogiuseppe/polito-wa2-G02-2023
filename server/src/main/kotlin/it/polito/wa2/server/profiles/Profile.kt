package it.polito.wa2.server.profiles

import it.polito.wa2.server.ticketing.message.Message
import it.polito.wa2.server.ticketing.ticket.Ticket
import it.polito.wa2.server.ticketing.tickethistory.TicketHistory
import jakarta.persistence.*

@Entity
@Table(name="profiles")
class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "profile_generator")
    @SequenceGenerator(name = "profile_generator",
        sequenceName = "profiles_id_seq",
        initialValue = 1,
        allocationSize = 1
    )
    @Column(updatable = false, nullable = false)
    var profileId : Long? = null
    @Column(nullable=false, unique = true)
    var email: String = ""
    @Column(nullable = false)
    var name: String = ""
    @Column(nullable = false)
    var surname: String = ""
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    var role: ProfileRole? = null

    @OneToMany(mappedBy = "customer")
    val ticketsCustomer = mutableSetOf<Ticket>()

    @OneToMany(mappedBy = "expert")
    val ticketsExpert = mutableSetOf<Ticket>()

    @OneToMany(mappedBy = "sender")
    val messagesSender = mutableSetOf<Message>()

    @OneToMany(mappedBy = "user")
    val historyEditor = mutableSetOf<TicketHistory>()

    @OneToMany(mappedBy = "currentExpert")
    val historyExpert = mutableSetOf<TicketHistory>()
}

enum class ProfileRole {
    CLIENT, EXPERT, MANAGER
}