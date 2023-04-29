package it.polito.wa2.server.products

import it.polito.wa2.server.ticketing.ticket.Ticket
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name="products")
class Product {
    @Id
    var productId: String = ""
    var name: String = ""
    var brand: String = ""

    @OneToMany(mappedBy = "product_id")
    val tickets = mutableSetOf<Ticket>()
}