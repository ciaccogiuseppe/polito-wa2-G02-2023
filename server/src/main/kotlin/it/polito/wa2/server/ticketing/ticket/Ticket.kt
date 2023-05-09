package it.polito.wa2.server.ticketing.ticket

import it.polito.wa2.server.products.Product
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.ticketing.message.Message
import it.polito.wa2.server.ticketing.tickethistory.TicketHistory
import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name="tickets")
class Ticket {
    @Column(nullable = false)
    var title : String = ""
    @Column(nullable = false)
    var description : String = ""
    @Column(nullable = false)
    var priority : Int = 0
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    var status : TicketStatus = TicketStatus.OPEN

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    var createdTimestamp : Timestamp? = null

    @ManyToOne
    @JoinColumn(name="product_id")
    @Column(nullable = false)
    var product : Product? = null

    @ManyToOne
    @Column(nullable = false)
    var customer : Profile? = null

    @ManyToOne
    var expert : Profile? = null

    @OneToMany(mappedBy = "ticket")
    var messages = mutableListOf<Message>()

    @OneToMany(mappedBy = "ticket")
    var history = mutableListOf<TicketHistory>()


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ticket_generator")
    @SequenceGenerator(name = "ticket_generator",
        sequenceName = "tickets_id_seq",
        initialValue = 1,
        allocationSize = 1
    )
    @Column(updatable = false, nullable = false)
    var ticketId : Long? = null


}

enum class TicketStatus{
    OPEN, RESOLVED, CLOSED, IN_PROGRESS, REOPENED
}
