package it.polito.wa2.server.keycloak

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.BadRequestUserException
import it.polito.wa2.server.ForbiddenException
import it.polito.wa2.server.UnprocessableProfileException
import it.polito.wa2.server.UnprocessableUserException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@CrossOrigin(origins =["http://localhost:3001"])
@Observed
class KeycloakController(private val keycloakService: KeycloakService) {
    @PostMapping("/API/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun addClient(@Valid @RequestBody userDTO: UserDTO?, br: BindingResult) {
        checkInputUser(userDTO, br)
        keycloakService.addClient(userDTO!!)
    }

    @PostMapping("/API/createExpert")
    @ResponseStatus(HttpStatus.CREATED)
    fun addExpert(@Valid @RequestBody userDTO: UserDTO?, br: BindingResult) {
        checkInputUser(userDTO, br)
        keycloakService.addExpert(userDTO!!)
    }

    @PutMapping("/API/client/user/{email}")
    fun clientUpdateKCUser(principal: Principal, @PathVariable email: String, @Valid @RequestBody userDTO: UserDTO?, br: BindingResult) {
        val userEmail = retrieveUserEmail(principal)
        checkEmailWithLoggedUser(email, userEmail)
        checkInputUser(userDTO, br)
        keycloakService.updateUser(email, userDTO!!)
    }

    @PutMapping("/API/expert/user/{email}")
    fun expertUpdateKCUser(principal: Principal, @PathVariable email: String, @Valid @RequestBody userDTO: UserDTO?, br: BindingResult) {
        val userEmail = retrieveUserEmail(principal)
        checkEmailWithLoggedUser(email, userEmail)
        checkInputUser(userDTO, br)
        keycloakService.updateUser(email, userDTO!!)
    }

    @PutMapping("/API/manager/user/{email}")
    fun managerUpdateKCUser(principal: Principal, @PathVariable email: String, @Valid @RequestBody userDTO: UserDTO?, br: BindingResult) {
        checkEmail(email)
        checkInputUser(userDTO, br)
        keycloakService.updateUser(email, userDTO!!)
    }

    private fun checkInputUser(userDTO: UserDTO?, br: BindingResult){
        if (br.hasErrors())
            throw UnprocessableUserException("Wrong user format")
        if (userDTO == null)
            throw BadRequestUserException("User must not be NULL")
    }

    fun checkEmailWithLoggedUser(email: String, emailLogged: String){
        checkEmail(email)
        if(email != emailLogged)
            throw ForbiddenException("You cannot updated profiles that are not yours")
    }

    fun checkEmail(email: String){
        if (!email.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")))
            throw UnprocessableProfileException("Wrong email format")
    }

    fun retrieveUserEmail(principal: Principal): String {
        val token: JwtAuthenticationToken = principal as JwtAuthenticationToken
        return token.tokenAttributes["email"] as String
    }
}