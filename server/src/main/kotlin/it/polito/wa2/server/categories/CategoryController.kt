package it.polito.wa2.server.categories

import io.micrometer.observation.annotation.Observed
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins =["http://localhost:3001"])
@RestController
@Observed
class CategoryController(private val categoryService: CategoryService) {
    @GetMapping("/API/public/categories/")
    fun getAllCategories(): List<CategoryDTO> {
        return categoryService.getAllCategories()
    }
}