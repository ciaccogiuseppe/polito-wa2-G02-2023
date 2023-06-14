package it.polito.wa2.server.categories

import jakarta.validation.constraints.NotBlank

data class CategoryDTO(
    @field:NotBlank(message="category name is mandatory")
    val categoryName: ProductCategory
)

fun Category.toDTO(): CategoryDTO {
    return CategoryDTO(this.name)
}