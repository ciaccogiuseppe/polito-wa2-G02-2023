package it.polito.wa2.server.items

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.products.Product
import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.products.ProductService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service @Transactional @Observed
class ItemServiceImpl(
    private val productRepository: ProductRepository,
    private val productService: ProductService,
    private val itemRepository: ItemRepository
): ItemService {
    @Transactional(readOnly = true)
    override fun getItemByProductId(productId: String): List<ItemDTO> {
        val product = getProduct(productId)
        return itemRepository.findAllByProduct(product).map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    override fun getItemByProductIdAndSerialNum(productId: String, serialNum: Long): ItemDTO? {
        val product = getProduct(productId)
        return itemRepository.findByProductAndSerialNum(product, serialNum)?.toDTO()
    }

    override fun addItem(itemDTO: ItemDTO): ItemDTO {
        val product = getProduct(itemDTO.productId)
        val newItem = itemDTO.toNewItem(product)
        val item = itemRepository.save(newItem)
        product.items.add(item)
        product.serialNumGen = product.serialNumGen + 1
        productRepository.save(product)
        return item.toDTO()
    }

    private fun getProduct(productId: String): Product {
        val productDTO = productService.getProduct(productId)
        return productRepository.findByIdOrNull(productDTO.productId)!!
    }
}