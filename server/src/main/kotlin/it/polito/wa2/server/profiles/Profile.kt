package it.polito.wa2.server.profiles

import it.polito.wa2.server.EntityBase
import it.polito.wa2.server.ticketing.message.Message
import it.polito.wa2.server.ticketing.ticket.Ticket
import it.polito.wa2.server.ticketing.tickethistory.TicketHistory
import jakarta.persistence.*

@Entity
@Table(name="profiles")
class Profile : EntityBase<Long>() {

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