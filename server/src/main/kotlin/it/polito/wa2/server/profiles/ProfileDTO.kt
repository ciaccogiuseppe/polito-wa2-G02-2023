package it.polito.wa2.server.profiles

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class ProfileDTO(
    @field:NotBlank(message="email is mandatory")
    @field:Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$",
        message="email must be valid")
    val email: String,
    @field:NotBlank(message="name is mandatory")
    val name: String,
    @field:NotBlank(message="surname is mandatory")
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