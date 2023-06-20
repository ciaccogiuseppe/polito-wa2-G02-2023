package it.polito.wa2.server.addresses

interface AddressService {
    fun getAddressOfClient(email: String): AddressDTO?
    fun updateAddressOfClient(email: String, newAddress: AddressDTO)
    fun addAddress(email: String, newAddress: AddressDTO)
}