package it.polito.wa2.server.products

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ProductDTO(
    @field:Size(min = 13, max = 13)
    val productId: String,
    @field:NotNull
    val name: String,
    @field:NotNull
    val brand: String
)

fun Product.toDTO(): ProductDTO {
    return ProductDTO(productId, name, brand)
}