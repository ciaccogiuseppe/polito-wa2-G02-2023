package it.polito.wa2.server.products

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name="products")
class Product {
    @Id
    var product_id: String = ""
    var name: String = ""
    var brand: String = ""
}