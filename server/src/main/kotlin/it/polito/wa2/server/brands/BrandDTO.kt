package it.polito.wa2.server.brands

import jakarta.validation.constraints.NotBlank

data class BrandDTO(
    @field:NotBlank(message="brand name is mandatory")
    val name: String
)

fun Brand.toDTO(): BrandDTO {
    return BrandDTO(this.name)
}

fun BrandDTO.toNewBrand() : Brand {
    val brand = Brand()
    brand.name = this.name
    return brand
}