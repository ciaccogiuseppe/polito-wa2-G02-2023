package it.polito.wa2.server.products

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.*
import it.polito.wa2.server.brands.BrandRepository
import it.polito.wa2.server.categories.CategoryRepository
import it.polito.wa2.server.categories.ProductCategory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.IllegalArgumentException

@Service @Transactional(readOnly = true) @Observed
class ProductServiceImpl(
    private val productRepository: ProductRepository,
    private val brandRepository: BrandRepository,
    private val categoryRepository: CategoryRepository
): ProductService {
    override fun getAllProducts(): List<ProductDTO> {
        return productRepository.findAll().map {it.toDTO()}
    }

    override fun getProduct(productId: String): ProductDTO {
        return productRepository.findByIdOrNull(productId)?.toDTO()
            ?: throw ProductNotFoundException("Product with id '${productId}' not found")
    }

    @Transactional(readOnly = false)
    override fun addProduct(productDTO: ProductDTO): ProductDTO {
        if (productRepository.findByIdOrNull(productDTO.productId) != null)
            throw DuplicateProductException("Product with id '${productDTO.productId}' already exists")

        val brand = brandRepository.findByName(productDTO.brand)
        if(brand === null)
            throw BrandNotFoundException("Brand with name '${productDTO.brand}' not found")
        try {
            val category = categoryRepository.findByName(ProductCategory.valueOf(productDTO.category))
            if(category === null)
                throw CategoryNotFoundException("Category with name '${productDTO.category}' not found")
            val product = productRepository.save(productDTO.toNewProduct(brand, category))
            return product.toDTO()
        }
        catch (e:IllegalArgumentException){
            throw CategoryNotFoundException("Category with name '${productDTO.category}' not found")
        }


    }
}