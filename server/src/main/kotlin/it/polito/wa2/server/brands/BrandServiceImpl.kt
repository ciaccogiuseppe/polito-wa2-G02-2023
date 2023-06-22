package it.polito.wa2.server.brands

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.BrandNotFoundException
import it.polito.wa2.server.DuplicateBrandException
import it.polito.wa2.server.security.WebSecurityConfig
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
@Observed
class BrandServiceImpl(private val brandRepository: BrandRepository): BrandService{
    @Transactional(readOnly = true)
    override fun getBrand(name: String): BrandDTO {
        return brandRepository.findByName(name)?.toDTO()
            ?: throw BrandNotFoundException("Brand with name '${name}' not found")
    }

    @Transactional(readOnly = true)
    override fun getAllBrands(): List<BrandDTO> {
        return brandRepository.findAll().map { it.toDTO() }
    }

    @PreAuthorize("hasRole('${WebSecurityConfig.MANAGER}')")
    override fun addBrand(brandDTO: BrandDTO): BrandDTO {
        if (brandRepository.findByName(brandDTO.name) != null)
            throw DuplicateBrandException("Brand with name '${brandDTO.name}' already exists")

        return brandRepository.save(brandDTO.toNewBrand()).toDTO()
    }
}