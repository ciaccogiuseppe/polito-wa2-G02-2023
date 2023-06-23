package it.polito.wa2.server.products

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.*
import it.polito.wa2.server.brands.BrandRepository
import it.polito.wa2.server.categories.CategoryRepository
import it.polito.wa2.server.security.WebSecurityConfig
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
@Observed
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val brandRepository: BrandRepository,
    private val categoryRepository: CategoryRepository
) : ProductService {
    override fun getAllProducts(): List<ProductDTO> {
        return productRepository.findAll().map { it.toDTO() }
    }

    override fun getProduct(productId: String): ProductDTO {
        return productRepository.findByIdOrNull(productId)?.toDTO()
            ?: throw ProductNotFoundException("Product with id '${productId}' not found")
    }

    @Transactional(readOnly = false)
    @PreAuthorize("hasRole('${WebSecurityConfig.MANAGER}')")
    override fun addProduct(productDTO: ProductDTO): ProductDTO {
        if (productRepository.findByIdOrNull(productDTO.productId) != null)
            throw DuplicateProductException("Product with id '${productDTO.productId}' already exists")

        val brand = brandRepository.findByName(productDTO.brand)
            ?: throw BrandNotFoundException("Brand with name '${productDTO.brand}' not found")
        val category = categoryRepository.findByName(productDTO.category)
            ?: throw CategoryNotFoundException("Category with name '${productDTO.category}' not found")
        val product = productRepository.save(productDTO.toNewProduct(brand, category))
        return product.toDTO()

    }
}