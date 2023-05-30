package it.polito.wa2.server.keycloak

import it.polito.wa2.server.BadRequestUserException
import it.polito.wa2.server.UnprocessableUserException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@RestController
class KeycloakController(private val keycloakService: KeycloakService) {
    @PostMapping("/API/public/clients")
    @ResponseStatus(HttpStatus.CREATED)
    fun addClient(@Valid @RequestBody userDTO: UserDTO?, br: BindingResult){
        checkInputUser(userDTO, br)
        keycloakService.addClient(userDTO!!)
    }

    @PostMapping("/API/manager/experts")
    @ResponseStatus(HttpStatus.CREATED)
    fun addExpert(@Valid @RequestBody userDTO: UserDTO?, br: BindingResult){
        checkInputUser(userDTO, br)
        keycloakService.addExpert(userDTO!!)
    }

    private fun checkInputUser(userDTO: UserDTO?, br: BindingResult){
        if (br.hasErrors())
            throw UnprocessableUserException("Wrong user format")
        if (userDTO == null)
            throw BadRequestUserException("User must not be NULL")
    }
}