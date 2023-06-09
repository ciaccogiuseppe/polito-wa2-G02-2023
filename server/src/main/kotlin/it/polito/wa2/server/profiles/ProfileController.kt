package it.polito.wa2.server.profiles

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.BadRequestProfileException
import it.polito.wa2.server.ForbiddenException
import it.polito.wa2.server.UnprocessableProfileException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import java.security.Principal

@CrossOrigin(origins =["http://localhost:3000"])
@RestController
@Observed
class ProfileController(private val profileService: ProfileService) {
    @GetMapping("/API/manager/profiles/{email}")
    fun getProfile(@PathVariable email: String): ProfileDTO {
        checkEmail(email)
        return profileService.getProfile(email)
    }

    @GetMapping("/API/manager/profiles/profileId/{profileId}")
    fun getProfileById(@PathVariable profileId: Long): ProfileDTO {
        if(profileId<=0)
            throw UnprocessableProfileException("Wrong profileId values")
        return profileService.getProfileById(profileId)
    }


    @PutMapping("/API/manager/profiles/{email}")
    fun managerUpdateProfile(@PathVariable email: String, @RequestBody @Valid profileDTO: ProfileDTO?, br: BindingResult) {
        checkEmail(email)
        checkInputProfile(profileDTO, br)
        profileService.updateProfile(email, profileDTO!!)
    }

    @PutMapping("/API/client/profiles/{email}")
    fun clientUpdateProfile(principal: Principal, @PathVariable email: String, @RequestBody @Valid profileDTO: ProfileDTO?, br: BindingResult) {
        val token: JwtAuthenticationToken = principal as JwtAuthenticationToken
        val userEmail = token.tokenAttributes["email"] as String
        checkEmailWithLoggedUser(email, userEmail)
        checkInputProfile(profileDTO, br)
        profileService.updateProfile(email, profileDTO!!)
    }

    @PutMapping("/API/expert/profiles/{email}")
    fun expertUpdateProfile(principal: Principal, @PathVariable email: String, @RequestBody @Valid profileDTO: ProfileDTO?, br: BindingResult) {
        val token: JwtAuthenticationToken = principal as JwtAuthenticationToken
        val userEmail = token.tokenAttributes["email"] as String
        checkEmailWithLoggedUser(email, userEmail)
        checkInputProfile(profileDTO, br)
        profileService.updateProfile(email, profileDTO!!)
    }

    fun checkInputProfile(profileDTO: ProfileDTO?, br: BindingResult){
        if (br.hasErrors())
            throw UnprocessableProfileException("Wrong profile format")
        if (profileDTO == null)
            throw BadRequestProfileException("Profile must not be NULL")
    }

    fun checkEmail(email: String){
        if (!email.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")))
            throw UnprocessableProfileException("Wrong email format")
    }

    fun checkEmailWithLoggedUser(email: String, emailLogged: String){
        if (!email.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")))
            throw UnprocessableProfileException("Wrong email format")
        if(email != emailLogged)
            throw ForbiddenException("You cannot updated profiles that are not yours")
    }
}