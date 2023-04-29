package it.polito.wa2.server.ticketing.Ticket

import it.polito.wa2.server.products.Product
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.ticketing.Message.Message
import it.polito.wa2.server.ticketing.TicketHistory.TicketHistory
import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name="tickets")
class Ticket {
    var title : String = ""
    var description : String = ""
    var priority : Int = 0
    var status : String = ""


    @Temporal(TemporalType.TIMESTAMP)
    var timestamp : Timestamp? = null

    @ManyToOne
    var product_id : Product? = null

    @ManyToOne
    var customer_id : Profile? = null

    @ManyToOne
    var expert_id : Profile? = null

    @OneToMany(mappedBy = "ticket_id")
    var messages = mutableListOf<Message>()

    @OneToMany(mappedBy = "ticket_id")
    var history = mutableListOf<TicketHistory>()


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ticket_generator")
    @SequenceGenerator(name = "ticket_generator",
        sequenceName = "tickets_id_seq",
        initialValue = 1,
        allocationSize = 1
    )
    var ticketId : Long? = null


}