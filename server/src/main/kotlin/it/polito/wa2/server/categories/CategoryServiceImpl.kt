package it.polito.wa2.server.categories

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.CategoryNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
@Observed
class CategoryServiceImpl(private val categoryRepository: CategoryRepository) : CategoryService {

    override fun getCategory(categoryName: ProductCategory): CategoryDTO {
        return categoryRepository.findByName(categoryName)?.toDTO()
            ?: throw CategoryNotFoundException("Category with name '${categoryName}' not found")
    }

    override fun getAllCategories(): List<CategoryDTO> {
        return categoryRepository.findAll().map { it.toDTO() }
    }
}