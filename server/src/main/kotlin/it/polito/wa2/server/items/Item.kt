package it.polito.wa2.server.items

import it.polito.wa2.server.EntityBase
import it.polito.wa2.server.products.Product
import it.polito.wa2.server.profiles.Profile
import jakarta.persistence.*

@Entity
@Table(name="items")
@IdClass(ItemId::class)
class Item {

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @Id
    var product: Product?= null

    @Id
    /*
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "serial_num_gen")
    @TableGenerator(name="serial_num_gen",
        table="products",
        pkColumnName = "product_id",
        valueColumnName = "serial_num_gen")
    @Column(name = "serial_num")
     */
    var serialNum: Long? = null

    var uuid: String? = null

    @ManyToOne
    @JoinColumn(nullable = true)
    var client: Profile? = null
}