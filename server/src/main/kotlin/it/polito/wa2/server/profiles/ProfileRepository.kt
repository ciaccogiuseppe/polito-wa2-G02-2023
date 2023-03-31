package it.polito.wa2.server.profiles

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProfileRepository: JpaRepository<Profile,String> {
    fun findByEmail(email: String): List<Profile>
}