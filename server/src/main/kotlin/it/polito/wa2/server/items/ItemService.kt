package it.polito.wa2.server.items

interface ItemService {
    fun getItemByProductId(productId: String): List<ItemDTO>

    fun getItemByProductIdAndSerialNum(productId: String, serialNum: Long): ItemDTO?

    fun addItem(itemDTO: ItemDTO): ItemDTO
}