package it.polito.wa2.server.items

import it.polito.wa2.server.products.Product
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.ticketing.ticket.Ticket
import jakarta.persistence.*
import java.sql.Timestamp
import java.util.*

@Entity
@Table(name = "items")
@IdClass(ItemId::class)
class Item {

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @Id
    var product: Product? = null

    @Id
    @Column(name = "serial_num")
    var serialNum: Long? = null

    var uuid: UUID? = null

    @Temporal(TemporalType.TIMESTAMP)
    var validFromTimestamp: Timestamp? = null

    var durationMonths: Long? = null

    @ManyToOne
    @JoinColumn(nullable = true)
    var client: Profile? = null

    @OneToMany(mappedBy = "item")
    var tickets = mutableSetOf<Ticket>()
}