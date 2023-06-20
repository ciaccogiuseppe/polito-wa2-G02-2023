package it.polito.wa2.server.addresses

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.AddressNotFoundException
import it.polito.wa2.server.DuplicateAddressException
import it.polito.wa2.server.profiles.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
@Observed
class AddressServiceImpl(
    private val addressRepository: AddressRepository,
    private val profileRepository: ProfileRepository
): AddressService {
    override fun getAddressOfClient(email: String): AddressDTO? {
        val address = addressRepository.findByClient(getProfileByEmail(email)) ?: return null
        return address.toDTO()
    }

    override fun updateAddressOfClient(email: String, newAddress: AddressDTO) {
        val addr= addressRepository.findByClient(getProfileByEmail(email))
            ?: throw AddressNotFoundException("Address not found")

        addr.country = newAddress.country
        addr.region = newAddress.region
        addr.city = newAddress.city
        addr.address = newAddress.address
        addressRepository.save(addr)
    }

    override fun addAddress(email: String, newAddress: AddressDTO) {
        val client = getProfileByEmail(email)
        if (addressRepository.findByClient(client) != null)
            throw DuplicateAddressException("The address already exists")

        addressRepository.save(newAddress.toNewAddress(client))
    }

    private fun getProfileByEmail(email: String): Profile {
        return profileRepository.findByEmail(email)!!
    }
}