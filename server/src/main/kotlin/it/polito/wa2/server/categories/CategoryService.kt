package it.polito.wa2.server.categories

interface CategoryService {
    fun getCategory(categoryName: ProductCategory): CategoryDTO
    fun getAllCategories(): List<CategoryDTO>
}