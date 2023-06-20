package it.polito.wa2.server.brands

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.BadRequestBrandException
import it.polito.wa2.server.UnprocessableBrandException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import java.security.Principal

@CrossOrigin(origins =["http://localhost:3001"])
@RestController
@Observed

class BrandController (private val brandService : BrandService) {
    @GetMapping("/API/public/brands/")
    fun getAllBrands(): List<BrandDTO> {
        return brandService.getAllBrands()
    }

    @PostMapping("/API/manager/brand/")
    @ResponseStatus(HttpStatus.CREATED)
    fun addBrand(principal: Principal, @RequestBody @Valid brandDTO: BrandDTO?, br: BindingResult): BrandDTO {
        checkAddParameters(brandDTO, br)
        return brandService.addBrand(brandDTO!!)
    }


    fun checkAddParameters(brandDTO: BrandDTO?, br: BindingResult) {
        if (br.hasErrors())
            throw UnprocessableBrandException("Wrong brand format")
        if (brandDTO == null)
            throw BadRequestBrandException("Brand must not be NULL")
    }
}