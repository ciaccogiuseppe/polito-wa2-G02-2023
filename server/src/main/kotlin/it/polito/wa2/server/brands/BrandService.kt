package it.polito.wa2.server.brands

interface BrandService {
    fun getBrand(name: String): BrandDTO
    fun getAllBrands(): List<BrandDTO>

    fun addBrand(brandDTO:BrandDTO) : BrandDTO
}