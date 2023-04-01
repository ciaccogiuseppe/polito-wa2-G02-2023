package it.polito.wa2.server.profiles

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull

data class ProfileDTO(
    @field: NotNull
    @field:Email
    val email: String,
    @field:NotNull
    val name: String,
    @field:NotNull
    val surname: String
)

fun Profile.toDTO(): ProfileDTO {
    return ProfileDTO(email, name, surname)
}

fun ProfileDTO.toProfile(): Profile {
    val profile = Profile()
    profile.email = email
    profile.name = name
    profile.surname = surname
    return profile
}