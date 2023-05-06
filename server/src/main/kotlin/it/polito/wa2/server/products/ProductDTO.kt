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

fun ProductDTO.toProduct(): Product {
    val product = Product()
    product.productId = productId
    product.name = name
    product.brand = brand
    return product
}

fun Product.toDTO(): ProductDTO {
    return ProductDTO(productId, name, brand)
}