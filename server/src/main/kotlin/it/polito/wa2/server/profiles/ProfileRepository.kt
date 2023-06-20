package it.polito.wa2.server.profiles

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProfileRepository: JpaRepository<Profile,Long> {
    fun findByEmail(email: String): Profile?
    fun findByRole(role: ProfileRole) : List<Profile>
}