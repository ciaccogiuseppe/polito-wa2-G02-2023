package it.polito.wa2.server.brands

import it.polito.wa2.server.EntityBase
import it.polito.wa2.server.categories.ProductCategory
import it.polito.wa2.server.products.Product
import it.polito.wa2.server.profiles.Profile
import jakarta.persistence.*

@Entity
@Table(name="brands")
class Brand : EntityBase<Long>() {

    @Column(nullable = false, unique = true)
    var name: String = ""

    @OneToMany(mappedBy = "brand")
    var products: MutableSet<Product> = mutableSetOf()

}