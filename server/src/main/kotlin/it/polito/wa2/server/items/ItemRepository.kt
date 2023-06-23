package it.polito.wa2.server.items

import it.polito.wa2.server.products.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ItemRepository : JpaRepository<Item, Long> {
    fun findAllByProduct(product: Product): List<Item>

    fun findByProductAndSerialNum(product: Product, serialNum: Long): Item?
}