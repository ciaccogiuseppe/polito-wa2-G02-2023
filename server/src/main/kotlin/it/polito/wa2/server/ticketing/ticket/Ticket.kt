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
    var title : String = ""
    var description : String = ""
    var priority : Int = 0
    var status : String = ""


    @Temporal(TemporalType.TIMESTAMP)
    var createdTimestamp : Timestamp? = null

    @ManyToOne
    @JoinColumn(name="product_id")
    var product : Product? = null

    @ManyToOne
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
    var ticketId : Long? = null


}