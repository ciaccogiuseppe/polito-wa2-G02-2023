package it.polito.wa2.server.products

interface ProductService {
    fun getAllProducts(): List<ProductDTO>

    fun getProduct(productId: String): ProductDTO
}