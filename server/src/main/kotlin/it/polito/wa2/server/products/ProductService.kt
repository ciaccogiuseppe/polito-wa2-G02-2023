package it.polito.wa2.server.products

import it.polito.wa2.server.items.Item

interface ProductService {
    fun getAllProducts(): List<ProductDTO>

    fun getProduct(productId: String): ProductDTO

    fun addItem(productId:String, item: Item)

    fun addProduct(productDTO: ProductDTO): ProductDTO
}