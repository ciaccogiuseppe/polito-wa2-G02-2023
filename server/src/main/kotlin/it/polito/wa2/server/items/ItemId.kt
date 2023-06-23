package it.polito.wa2.server.items

import it.polito.wa2.server.products.Product
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.io.Serializable

class ItemId : Serializable {
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    var product: Product? = null

    var serialNum: Long? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemId

        if (product != other.product) return false
        return serialNum == other.serialNum
    }

    override fun hashCode(): Int {
        return 31
    }
}