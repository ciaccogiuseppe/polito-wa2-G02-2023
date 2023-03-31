package it.polito.wa2.server.products

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ProductDTO(
    @Size(min = 13, max = 13)
    val productId: String,
    @NotNull
    val name: String,
    @NotNull
    val brand: String
)

fun Product.toDTO(): ProductDTO {
    return ProductDTO(product_id, name, brand)
}