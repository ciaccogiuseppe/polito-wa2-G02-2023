package it.polito.wa2.server.products

import it.polito.wa2.server.brands.Brand
import it.polito.wa2.server.categories.Category
import it.polito.wa2.server.categories.ProductCategory
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ProductDTO(
    @field:Size(min = 13, max = 13)
    val productId: String,
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val brand: String,
    @field:NotNull
    val category: ProductCategory
)

fun Product.toDTO(): ProductDTO {
    return ProductDTO(productId, name, brand!!.name, category!!.name)
}

fun ProductDTO.toNewProduct(brand: Brand, category: Category): Product {
    val product = Product()
    product.brand = brand
    product.category = category
    product.productId = this.productId
    product.name = this.name

    return product
}