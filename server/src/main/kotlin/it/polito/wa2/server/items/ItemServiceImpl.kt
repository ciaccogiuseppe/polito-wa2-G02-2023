package it.polito.wa2.server.items

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.*
import it.polito.wa2.server.products.Product
import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.products.ProductService
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileService
import it.polito.wa2.server.security.WebSecurityConfig
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
@Observed
class ItemServiceImpl(
    private val productRepository: ProductRepository,
    private val productService: ProductService,
    private val profileRepository: ProfileRepository,
    private val profileService: ProfileService,
    private val itemRepository: ItemRepository
) : ItemService {
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('${WebSecurityConfig.MANAGER}', '${WebSecurityConfig.VENDOR}')")
    override fun getItemByProductId(productId: String): List<ItemDTO> {
        val product = getProduct(productId)
        return itemRepository.findAllByProduct(product).map { it.toDTO() }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('${WebSecurityConfig.MANAGER}', '${WebSecurityConfig.VENDOR}')")
    override fun getItemByProductIdAndSerialNum(productId: String, serialNum: Long): ItemDTO {
        val product = getProduct(productId)
        return itemRepository.findByProductAndSerialNum(product, serialNum)?.toDTO()
            ?: throw ItemNotFoundException("Item with productIid '${productId}' and serialNum '${serialNum}' not found")
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('${WebSecurityConfig.CLIENT}')")
    override fun getItemClient(productId: String, serialNum: Long, email: String): ItemDTO {
        val product = getProduct(productId)
        val item = itemRepository.findByProductAndSerialNum(product, serialNum)
            ?: throw ItemNotFoundException("Item with productIid '${productId}' and serialNum '${serialNum}' not found")
        if (item.client != getProfileByEmail(email, email))
            throw ForbiddenException("You cannot access to this item")
        return item.toDTO()
    }


    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('${WebSecurityConfig.CLIENT}', '${WebSecurityConfig.MANAGER}')")
    override fun getProfileItems(userEmail: String): List<ItemDTO> {
        val profile = profileRepository.findByEmail(userEmail)!!
        return profile.items.map { it.toDTO() }
    }

    @PreAuthorize("hasRole('${WebSecurityConfig.VENDOR}')")
    override fun addItem(productId: String, itemDTO: ItemDTO): ItemDTO {
        if (productId != itemDTO.productId)
            throw BadRequestItemException("ProductId in path doesn't match the productId in the body")
        val product = getProduct(itemDTO.productId)
        if (itemRepository.findByProductAndSerialNum(product, itemDTO.serialNum) != null)
            throw DuplicateItemException("Item with productId '${itemDTO.productId}' and serialNum '${itemDTO.serialNum}'already exists")
        val newItem = itemDTO.toNewItem(product)
        val item = itemRepository.save(newItem)
        productService.addItem(product.productId, item)
        return item.toDTO()
    }


    @PreAuthorize("hasRole('${WebSecurityConfig.VENDOR}')")
    override fun createUUID(itemDTO: ItemDTO): ItemDTO {
        if (itemDTO.durationMonths == null)
            throw UnprocessableItemException("Duration cannot be null")
        val product = getProduct(itemDTO.productId)
        val item = itemRepository.findByProductAndSerialNum(product, itemDTO.serialNum)
            ?: itemRepository.save(itemDTO.toNewItem(product))
        if (item.uuid != null)
            throw UnprocessableItemException("Item already has an UUID")
        item.uuid = UUID.randomUUID()
        item.validFromTimestamp = Timestamp.valueOf(LocalDateTime.now())
        item.durationMonths = itemDTO.durationMonths
        return itemRepository.save(item).toDTO()
    }

    @PreAuthorize("hasRole('${WebSecurityConfig.CLIENT}')")
    override fun assignClient(userEmail: String, itemDTO: ItemDTO): ItemDTO {
        if (itemDTO.uuid == null) {
            throw BadRequestItemException("UUID cannot be null")
        }
        val product = getProduct(itemDTO.productId)
        val item = itemRepository.findByProductAndSerialNum(product, itemDTO.serialNum)
            ?: throw ItemNotFoundException("Item with productIid '${itemDTO.productId}' and serialNum '${itemDTO.serialNum}' not found")
        if (itemDTO.uuid != item.uuid)
            throw ForbiddenException("Impossible to be linked to the product")
        item.client = getProfileByEmail(userEmail, userEmail)
        return itemRepository.save(item).toDTO()
    }

    private fun getProduct(productId: String): Product {
        val productDTO = productService.getProduct(productId)
        return productRepository.findByIdOrNull(productDTO.productId)!!
    }

    private fun getProfileByEmail(email: String, loggedEmail: String): Profile {
        val profileDTO = profileService.getProfile(email, loggedEmail)
        return profileRepository.findByEmail(profileDTO.email)!!
    }
}