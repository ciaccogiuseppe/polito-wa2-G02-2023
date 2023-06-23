package it.polito.wa2.server.ticketing.ticket

import it.polito.wa2.server.EntityBase
import it.polito.wa2.server.items.Item
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.ticketing.message.Message
import it.polito.wa2.server.ticketing.tickethistory.TicketHistory
import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name = "tickets")
class Ticket : EntityBase<Long>() {
    @Column(nullable = false)
    var title: String = ""

    @Column(nullable = false)
    var description: String = ""

    @Column(nullable = false)
    var priority: Int = 0

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    var status: TicketStatus = TicketStatus.OPEN

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var createdTimestamp: Timestamp? = null

    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "product_id", nullable = false),
        JoinColumn(name = "serial_num", nullable = false)
    )
    var item: Item? = null

    @ManyToOne
    @JoinColumn(nullable = false)
    var client: Profile? = null

    @ManyToOne
    var expert: Profile? = null

    @OneToMany(mappedBy = "ticket")
    var messages = mutableListOf<Message>()

    @OneToMany(mappedBy = "ticket")
    var history = mutableListOf<TicketHistory>()
}

enum class TicketStatus {
    OPEN, RESOLVED, CLOSED, IN_PROGRESS, REOPENED
}
