package it.polito.wa2.server.items

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.*
import jakarta.validation.Valid
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import java.security.Principal

@CrossOrigin(origins = ["http://localhost:3001"])
@RestController
@Observed
class ItemController(private val itemService: ItemService) {
    @GetMapping("/API/private/products/{productId}/items/")
    fun getItemByProductId(@PathVariable("productId") productId: String): List<ItemDTO> {
        checkProductId(productId)
        return itemService.getItemByProductId(productId)
    }

    @GetMapping("/API/private/products/{productId}/items/{serialNum}")
    fun getItemByProductIdAndSerialNum(@PathVariable("productId") productId: String,
                                       @PathVariable("serialNum") serialNum: Long): ItemDTO? {
        checkProductId(productId)
        checkSerialNum(serialNum)
        return itemService.getItemByProductIdAndSerialNum(productId, serialNum)
    }

    @GetMapping("/API/client/profiles/items")
    fun getProfileItems(principal: Principal): List<ItemDTO> {
        val email = retrieveUserEmail(principal)
        return itemService.getProfileItems(email)
    }

    @PostMapping("/API/vendor/products/items/")
    fun generateUUID(@RequestBody @Valid itemDTO: ItemDTO?, br: BindingResult): ItemDTO {
        checkInputItem(itemDTO, br)
        return itemService.createUUID(itemDTO!!)
    }

    @PutMapping("/API/client/products/items/register")
    fun assignClient(principal: Principal,
                     @RequestBody @Valid itemDTO: ItemDTO?, br: BindingResult): ItemDTO {
        val userEmail = retrieveUserEmail(principal)
        checkInputItem(itemDTO, br)
        return itemService.assignClient(userEmail, itemDTO!!)
    }

    private fun checkProductId(productId: String) {
        if (!productId.matches("\\d{13}".toRegex()))
            throw UnprocessableItemException("Wrong format for productId")
    }

    private fun checkSerialNum(serialNum: Long) {
        if (serialNum <= 0)
            throw UnprocessableItemException("Wrong format for serialNum")
    }

    private fun checkInputItem(itemDTO: ItemDTO?, br: BindingResult) {
        if (br.hasErrors())
            throw UnprocessableItemException("Wrong item format")
        if (itemDTO == null)
            throw BadRequestItemException("Item must not be NULL")
    }

    private fun retrieveUserEmail(principal: Principal): String {
        val token: JwtAuthenticationToken = principal as JwtAuthenticationToken
        return token.tokenAttributes["email"] as String
    }
}