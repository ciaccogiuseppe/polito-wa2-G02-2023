package it.polito.wa2.server.categories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository: JpaRepository<Category,Long> {
    fun findByName(categoryName: ProductCategory): Category?
}