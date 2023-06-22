package it.polito.wa2.server.keycloak

import it.polito.wa2.server.categories.ProductCategory
import it.polito.wa2.server.addresses.AddressDTO
import it.polito.wa2.server.profiles.ProfileDTO
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import java.util.UUID

data class UserDTO (
    @field:NotBlank(message="username is mandatory")
    val username: String,
    @field:NotBlank(message="email is mandatory")
    @field:Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$",
        message="email must be valid")
    val email: String,
    @field:NotBlank(message="password is mandatory")
    val password: String,
    @field:NotBlank(message="first name is mandatory")
    @field:Pattern(regexp = "([a-zA-Z]+'?\\s?)+",
        message="name must be valid")
    val firstName: String,
    @field:NotBlank(message="last name is mandatory")
    @field:Pattern(regexp = "([a-zA-Z]+'?\\s?)+",
        message="surname must be valid")
    val lastName: String,
    val expertCategories: Set<ProductCategory>?,
    val address: AddressDTO?,
    val role: String?
)

data class UserUpdateDTO (
    @field:NotBlank(message="email is mandatory")
    @field:Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$",
        message="email must be valid")
    val email: String,
    @field:NotBlank(message="first name is mandatory")
    @field:Pattern(regexp = "([a-zA-Z]+'?\\s?)+",
        message="name must be valid")
    val firstName: String,
    @field:NotBlank(message="last name is mandatory")
    @field:Pattern(regexp = "([a-zA-Z]+'?\\s?)+",
        message="surname must be valid")
    val lastName: String,
    val expertCategories: Set<ProductCategory>?,
    val address: AddressDTO?,
    val role: String?
)

data class PasswordDTO (

    @field:NotBlank(message="email is mandatory")
    @field:Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$",
        message="email must be valid")
    val email: String,
    @field:NotBlank(message="password is mandatory")
    val password:String,
    val token:UUID
)


fun UserDTO.toProfileDTO(): ProfileDTO {
    return ProfileDTO (this.email, this.firstName, this.lastName, null, expertCategories, this.address, this.role)
}

fun UserUpdateDTO.toProfileDTO(): ProfileDTO{
    return ProfileDTO (this.email, this.firstName, this.lastName, null, expertCategories, this.address, this.role)
}