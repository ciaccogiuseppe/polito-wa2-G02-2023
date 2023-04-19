package it.polito.wa2.server.profiles

import jakarta.persistence.*

@Entity
@Table(name="profiles")
class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "profile_generator")
    @SequenceGenerator(name = "profile_generator",
        sequenceName = "profiles_profile_id_seq",
        initialValue = 1,
        allocationSize = 1
    )
    var profileId = 0
    var email: String = ""
    var name: String = ""
    var surname: String = ""

}