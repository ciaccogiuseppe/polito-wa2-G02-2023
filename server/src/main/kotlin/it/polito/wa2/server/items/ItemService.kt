package it.polito.wa2.server.items

interface ItemService {
    fun getItemByProductId(productId: String): List<ItemDTO>

    fun getItemByProductIdAndSerialNum(productId: String, serialNum: Long): ItemDTO

    fun addItem(productId: String, itemDTO: ItemDTO): ItemDTO

    fun createUUID(itemDTO: ItemDTO): ItemDTO

    fun assignClient(userEmail: String, itemDTO: ItemDTO): ItemDTO
}