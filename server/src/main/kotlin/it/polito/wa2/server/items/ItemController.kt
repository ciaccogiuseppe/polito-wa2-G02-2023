package it.polito.wa2.server.items

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["http://localhost:3001"])
@RestController
@Observed
class ItemController(private val itemService: ItemService) {
    @GetMapping("/API/public/products/{productId}/items/")
    fun getItemByProductId(@PathVariable("productId") productId: String): List<ItemDTO> {
        checkProductId(productId)
        return itemService.getItemByProductId(productId)
    }

    @GetMapping("/API/public/products/{productId}/items/{serialNum}")
    fun getItemByProductIdAndSerialNum(@PathVariable("productId") productId: String,
                                       @PathVariable("serialNum") serialNum: Long): ItemDTO? {
        checkProductId(productId)
        if (serialNum <= 0)
            throw UnprocessableItemException("Wrong format for serialNum")
        return itemService.getItemByProductIdAndSerialNum(productId, serialNum)
    }

    @PostMapping("/API/public/products/{productId}/items/")
    @ResponseStatus(HttpStatus.CREATED)
    fun addItem(@PathVariable("productId") productId: String, @RequestBody @Valid itemDTO: ItemDTO?, br: BindingResult): ItemDTO {
        checkProductId(productId)
        checkInputItem(itemDTO, br)
        return itemService.addItem(itemDTO!!)
    }

    private fun checkProductId(productId: String) {
        if (!productId.matches("\\d{13}".toRegex()))
            throw UnprocessableItemException("Wrong format for productId")
    }

    private fun checkInputItem(itemDTO: ItemDTO?, br: BindingResult) {
        if (br.hasErrors())
            throw UnprocessableItemException("Wrong item format")
        if (itemDTO == null)
            throw BadRequestItemException("Item must not be NULL")
    }
}