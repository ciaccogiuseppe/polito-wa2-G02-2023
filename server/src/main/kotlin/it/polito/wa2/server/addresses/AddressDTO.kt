package it.polito.wa2.server.addresses

import it.polito.wa2.server.profiles.Profile

data class AddressDTO (
    val country: String?,
    var region: String?,
    var city: String?,
    var address: String?
)

fun Address.toDTO(): AddressDTO {
    return AddressDTO(this.country, this.region, this.city, this.address)
}

fun AddressDTO.toNewAddress(associatedClient: Profile): Address {
    val addr = Address()
    addr.country = country
    addr.region = region
    addr.city = city
    addr.address = address
    addr.client = associatedClient
    return addr
}