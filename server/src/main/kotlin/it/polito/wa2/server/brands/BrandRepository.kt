package it.polito.wa2.server.brands

import it.polito.wa2.server.categories.Category
import it.polito.wa2.server.categories.ProductCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BrandRepository: JpaRepository<Brand, Long> {
    fun findByName(name: String): Brand?
}