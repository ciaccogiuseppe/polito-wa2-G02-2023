package it.polito.wa2.server.profiles

import jakarta.persistence.*

@Entity
@Table(name="profiles")
class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var profile_id = 0
    var email: String = ""
    var name: String = ""
    var surname: String = ""

}