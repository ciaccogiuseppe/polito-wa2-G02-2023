package it.polito.wa2.server.addresses

import it.polito.wa2.server.profiles.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AddressRepository : JpaRepository<Address, Long> {
    fun findByClient(client: Profile): Address?
}