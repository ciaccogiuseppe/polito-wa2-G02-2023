package it.polito.wa2.server.profiles

import it.polito.wa2.server.BadRequestProfileException
import it.polito.wa2.server.UnprocessableProfileException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins =["http://localhost:3000"])
@RestController
class ProfileController(private val profileService: ProfileService) {
    @GetMapping("/API/profiles/{email}")
    fun getProfile(@PathVariable email: String): ProfileDTO {
        checkEmail(email)
        return profileService.getProfile(email)
    }

    @PostMapping("/API/profiles")
    @ResponseStatus(HttpStatus.CREATED)
    fun addProfile(@RequestBody @Valid profileDTO: ProfileDTO?, br: BindingResult) {
        checkInputProfile(profileDTO, br)
        profileService.addProfile(profileDTO!!)
    }

    @PutMapping("/API/profiles/{email}")
    fun updateProfile(@PathVariable email: String, @RequestBody @Valid profileDTO: ProfileDTO?, br: BindingResult) {
        checkEmail(email)
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
}