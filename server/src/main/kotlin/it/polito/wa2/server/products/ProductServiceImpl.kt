package it.polito.wa2.server.products

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.ProductNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service @Transactional(readOnly = true) @Observed
class ProductServiceImpl(private val productRepository: ProductRepository): ProductService {
    override fun getAllProducts(): List<ProductDTO> {
        return productRepository.findAll().map {it.toDTO()}
    }

    override fun getProduct(productId: String): ProductDTO {
        return productRepository.findByIdOrNull(productId)?.toDTO()
            ?: throw ProductNotFoundException("Product with id '${productId}' not found")
    }
}