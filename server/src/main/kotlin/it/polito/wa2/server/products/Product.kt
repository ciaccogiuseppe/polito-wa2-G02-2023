package it.polito.wa2.server.products

import it.polito.wa2.server.brands.Brand
import it.polito.wa2.server.categories.Category
import it.polito.wa2.server.items.Item
import jakarta.persistence.*

@Entity
@Table(name="products")
class Product {
    @Id
    @Column(updatable = false, nullable = false)
    var productId: String = ""
    @Column(nullable = false)
    var name: String = ""

    @ManyToOne
    @JoinColumn(nullable = false)
    var brand: Brand? = null

    @ManyToOne
    @JoinColumn(nullable = false)
    var category: Category? = null

    @OneToMany(mappedBy = "product")
    val items = mutableSetOf<Item>()

}