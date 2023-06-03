package it.polito.wa2.server.profiles

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive

data class ProfileDTO(
    @field:NotBlank(message="email is mandatory")
    @field:Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$",
        message="email must be valid")
    val email: String,
    @field:NotBlank(message="name is mandatory")
    @field:Pattern(regexp = "([a-zA-Z]+'?\\s?)+",
        message="name must be valid")
    val name: String,
    @field:NotBlank(message="surname is mandatory")
    @field:Pattern(regexp = "([a-zA-Z]+'?\\s?)+",
        message="surname must be valid")
    val surname: String,
    @field:Positive
    val profileId: Long?
)

fun Profile.toDTO(): ProfileDTO {
    return ProfileDTO(email, name, surname, this.getId())
}

fun ProfileDTO.toNewProfile(profileRole: ProfileRole): Profile {
    val profile = Profile()
    profile.email = email
    profile.name = name
    profile.surname = surname
    profile.role = profileRole
    return profile
}
