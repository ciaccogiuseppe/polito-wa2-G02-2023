package it.polito.wa2.server.products

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ProductDTO(
    @field:Size(min = 13, max = 13)
    val productId: String,
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val brand: String
)

fun Product.toDTO(): ProductDTO {
    return ProductDTO(productId, name, brand)
}