package it.polito.wa2.server.products

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductController(
        private val productService: ProductService
) {

    @GetMapping("/API/products/")
    fun getAllProducts(): List<ProductDTO> {
        return productService.getAllProducts()
    }

    @GetMapping("/API/products/{productId}")
    fun getProduct(@PathVariable productId: String): ProductDTO? {
        return productService.getProduct(productId)
    }
}