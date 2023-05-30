package it.polito.wa2.server.keycloak

interface KeycloakService {
    fun addClient(userDTO: UserDTO)

    fun addExpert(userDTO: UserDTO)
}