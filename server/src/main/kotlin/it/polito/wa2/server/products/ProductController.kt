package it.polito.wa2.server.products

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.UnprocessableProductException
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@CrossOrigin(origins =["http://localhost:3001"])
@RestController
@Observed
class ProductController(private val productService: ProductService) {
    @GetMapping("/API/public/products/")
    fun getAllProducts(): List<ProductDTO> = productService.getAllProducts()

    @GetMapping("/API/public/products/{productId}")
    fun getProduct(@PathVariable productId: String): ProductDTO {
        if (!productId.matches("\\d{13}".toRegex()))
            throw UnprocessableProductException("Wrong format for productId")
        return productService.getProduct(productId)
    }
}