package it.polito.wa2.server.items

interface ItemService {
    fun getItemByProductId(productId: String): List<ItemDTO>

    fun getItemByProductIdAndSerialNum(productId: String, serialNum: Long): ItemDTO

    fun getItemClient(productId: String, serialNum: Long, email: String): ItemDTO

    fun getProfileItems(userEmail: String): List<ItemDTO>

    fun addItem(productId: String, itemDTO: ItemDTO): ItemDTO

    fun createUUID(itemDTO: ItemDTO): ItemDTO

    fun assignClient(userEmail: String, itemDTO: ItemDTO): ItemDTO
}