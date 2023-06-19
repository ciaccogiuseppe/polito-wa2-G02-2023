package it.polito.wa2.server.items

import it.polito.wa2.server.products.Product
import jakarta.persistence.*
import java.io.Serializable

class ItemId: Serializable {
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product?= null

    var serialNum: Long? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemId

        if (product != other.product) return false
        if (serialNum != other.serialNum) return false

        return true
    }

    override fun hashCode(): Int {
        return 31
    }
}