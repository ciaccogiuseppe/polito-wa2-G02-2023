package it.polito.wa2.server.products

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.BadRequestProductException
import it.polito.wa2.server.UnprocessableProductException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import java.security.Principal

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

    @PostMapping("/API/manager/products/")
    @ResponseStatus(HttpStatus.CREATED)
    fun addProduct(principal: Principal, @RequestBody @Valid productDTO: ProductDTO?, br: BindingResult): ProductDTO {
        checkAddParameters(productDTO, br)
        return productService.addProduct(productDTO!!)
    }


    private fun checkAddParameters(productDTO: ProductDTO?, br: BindingResult) {
        if (br.hasErrors())
            throw UnprocessableProductException("Wrong product format")
        if (productDTO == null)
            throw BadRequestProductException("Product must not be NULL")
    }
}