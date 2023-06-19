package it.polito.wa2.server.items

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.BadRequestItemException
import it.polito.wa2.server.ForbiddenException
import it.polito.wa2.server.ItemNotFoundException
import it.polito.wa2.server.UnprocessableItemException
import it.polito.wa2.server.products.Product
import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.products.ProductService
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*

@Service @Transactional @Observed
class ItemServiceImpl(
    private val productRepository: ProductRepository,
    private val productService: ProductService,
    private val profileRepository: ProfileRepository,
    private val profileService: ProfileService,
    private val itemRepository: ItemRepository
): ItemService {
    @Transactional(readOnly = true)
    override fun getItemByProductId(productId: String): List<ItemDTO> {
        val product = getProduct(productId)
        return itemRepository.findAllByProduct(product).map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    override fun getItemByProductIdAndSerialNum(productId: String, serialNum: Long): ItemDTO {
        val product = getProduct(productId)
        return itemRepository.findByProductAndSerialNum(product, serialNum)?.toDTO()
            ?: throw ItemNotFoundException("Item with productIid '${productId}' and serialNum '${serialNum}' not found")
    }

    override fun addItem(productId: String, itemDTO: ItemDTO): ItemDTO {
        if(productId != itemDTO.productId)
            throw BadRequestItemException("ProductId in path doesn't match the productId in the body")
        val product = getProduct(itemDTO.productId)
        val newItem = itemDTO.toNewItem(product)
        val item = itemRepository.save(newItem)
        product.items.add(item)
        product.serialNumGen = product.serialNumGen + 1
        productRepository.save(product)
        return item.toDTO()
    }

    override fun createUUID(productId: String, serialNum: Long, itemDTO: ItemDTO): ItemDTO {
        if(productId != itemDTO.productId)
            throw BadRequestItemException("ProductId in path doesn't match the productId in the body")
        if(serialNum != itemDTO.serialNum)
            throw BadRequestItemException("SerialNum in path doesn't match the serialNum in the body")
        val product = getProduct(itemDTO.productId)
        val item = itemRepository.findByProductAndSerialNum(product, serialNum)
            ?: throw ItemNotFoundException("Item with productIid '${productId}' and serialNum '${serialNum}' not found")
        if(item.uuid != null)
            throw UnprocessableItemException("Item already has an UUID")
        item.uuid = UUID.randomUUID()
        item.validFromTimestamp = Timestamp.valueOf(LocalDateTime.now())
        return itemRepository.save(item).toDTO()
    }

    override fun assignClient(userEmail: String, productId: String, serialNum: Long, itemDTO: ItemDTO): ItemDTO {
        if(productId != itemDTO.productId)
            throw BadRequestItemException("ProductId in path doesn't match the productId in the body")
        if(serialNum != itemDTO.serialNum)
            throw BadRequestItemException("SerialNum in path doesn't match the serialNum in the body")
        if(itemDTO.uuid == null) {
            throw BadRequestItemException("UUID cannot be null")
        }
        val product = getProduct(itemDTO.productId)
        val item = itemRepository.findByProductAndSerialNum(product, serialNum)
            ?: throw ItemNotFoundException("Item with productIid '${productId}' and serialNum '${serialNum}' not found")
        if(itemDTO.uuid != item.uuid)
            throw ForbiddenException("Impossible to be linked to the product")
        item.client = getProfileByEmail(userEmail)
        return itemRepository.save(item).toDTO()
    }

    private fun getProduct(productId: String): Product {
        val productDTO = productService.getProduct(productId)
        return productRepository.findByIdOrNull(productDTO.productId)!!
    }

    private fun getProfileByEmail(email: String): Profile {
        val profileDTO = profileService.getProfile(email)
        return profileRepository.findByEmail(profileDTO.email)!!
    }
}