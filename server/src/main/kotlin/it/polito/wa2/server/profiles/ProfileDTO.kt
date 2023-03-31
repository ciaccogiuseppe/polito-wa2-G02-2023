package it.polito.wa2.server.profiles

data class ProfileDTO(
        val email: String,
        val name: String,
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