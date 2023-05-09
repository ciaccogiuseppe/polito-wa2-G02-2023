package it.polito.wa2.server.products

import it.polito.wa2.server.ticketing.ticket.Ticket
import jakarta.persistence.*

@Entity
@Table(name="products")
class Product {
    @Id
    @Column(updatable = false, nullable = false)
    var productId: String = ""
    @Column(nullable = false)
    var name: String = ""
    @Column(nullable = false)
    var brand: String = ""

    @OneToMany(mappedBy = "product")
    val tickets = mutableSetOf<Ticket>()
}