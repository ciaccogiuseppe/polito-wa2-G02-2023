package it.polito.wa2.server.addresses

import it.polito.wa2.server.EntityBase
import it.polito.wa2.server.profiles.Profile
import jakarta.persistence.*

@Entity
@Table(name="addresses")
class Address: EntityBase<Long>() {
    @Column(nullable = true)
    var country: String? = null
    @Column(nullable = true)
    var region: String? = null
    @Column(nullable = true)
    var city: String? = null
    @Column(nullable = true)
    var address: String? = null

    @OneToOne
    @JoinColumn(nullable = false)
    var client: Profile? = null
}