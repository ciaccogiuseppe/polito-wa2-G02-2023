package it.polito.wa2.server.items

import it.polito.wa2.server.products.Product
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.sql.Timestamp
import java.util.UUID

data class ItemDTO(
    @field:Size(min = 13, max = 13)
    val productId: String,
    @field:Positive
    val serialNum: Long,
    val uuid: UUID?,
    @field:Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$",
        message="email must be valid")
    val clientEmail: String?,
    val validFromTimestamp: Timestamp?,
    @field:Positive
    val durationMonths: Long?
)

fun Item.toDTO(): ItemDTO {
    return ItemDTO(product!!.productId, serialNum!!, uuid, client?.email, validFromTimestamp, durationMonths)
}

fun ItemDTO.toNewItem(product: Product): Item {
    val item = Item()
    item.product = product
    item.serialNum = serialNum
    return item
}
