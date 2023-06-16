package it.polito.wa2.server.brands

import it.polito.wa2.server.categories.CategoryDTO
import it.polito.wa2.server.categories.ProductCategory

interface BrandService {
    fun getBrand(name: String): BrandDTO
    fun getAllBrands(): List<BrandDTO>

    fun addBrand(brandDTO:BrandDTO) : BrandDTO
}