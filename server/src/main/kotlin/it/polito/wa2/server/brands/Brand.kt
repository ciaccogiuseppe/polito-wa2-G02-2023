package it.polito.wa2.server.brands

import it.polito.wa2.server.EntityBase
import it.polito.wa2.server.products.Product
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "brands")
class Brand : EntityBase<Long>() {

    @Column(nullable = false, unique = true)
    var name: String = ""

    @OneToMany(mappedBy = "brand")
    var products: MutableSet<Product> = mutableSetOf()

}