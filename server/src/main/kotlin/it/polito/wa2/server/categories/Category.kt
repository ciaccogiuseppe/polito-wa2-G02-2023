package it.polito.wa2.server.categories

import it.polito.wa2.server.EntityBase
import it.polito.wa2.server.products.Product
import it.polito.wa2.server.profiles.Profile
import jakarta.persistence.*

@Entity
@Table(name = "categories")
class Category : EntityBase<Long>() {
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    var name: ProductCategory = ProductCategory.OTHER

    @OneToMany(mappedBy = "category")
    var products: MutableSet<Product> = mutableSetOf()

    @ManyToMany(mappedBy = "expertCategories")
    var experts: MutableSet<Profile> = mutableSetOf()
}

enum class ProductCategory {
    SMARTPHONE, TV, PC, SOFTWARE, STORAGE_DEVICE, OTHER
}