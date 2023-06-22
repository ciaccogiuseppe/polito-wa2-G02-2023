package it.polito.wa2.server.addresses

import it.polito.wa2.server.profiles.Profile
import jakarta.validation.constraints.NotBlank

data class AddressDTO (
    @field:NotBlank(message="address country is mandatory")
    val country: String?,
    @field:NotBlank(message="address region is mandatory")
    var region: String?,
    @field:NotBlank(message="address city is mandatory")
    var city: String?,
    @field:NotBlank(message="address is mandatory")
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