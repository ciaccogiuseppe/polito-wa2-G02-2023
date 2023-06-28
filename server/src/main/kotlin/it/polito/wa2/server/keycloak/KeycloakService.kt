package it.polito.wa2.server.keycloak

import java.util.UUID

interface KeycloakService {
    fun addClient(userDTO: UserDTO)

    fun addExpert(userDTO: UserDTO)

    fun resetPassword(email: String)
    fun validateEmail(email: String)
    fun applyResetPassword(passwordDTO: PasswordDTO)
    fun applyValidateEmail(token: UUID)
    fun addVendor(userDTO: UserDTO)

    fun updateUser(email: String, userDTO: UserUpdateDTO)
}