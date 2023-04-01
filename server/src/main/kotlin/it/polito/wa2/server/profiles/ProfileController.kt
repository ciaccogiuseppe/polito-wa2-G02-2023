package it.polito.wa2.server.profiles

import it.polito.wa2.server.BadRequestProfileException
import it.polito.wa2.server.DuplicateProfileException
import it.polito.wa2.server.ProfileNotFoundException
import it.polito.wa2.server.UnprocessableProfileException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins =["http://localhost:3000"])
@RestController
class ProfileController(private val profileService: ProfileService) {
    @GetMapping("/API/profiles/{email}")
    fun getProfile(@PathVariable email: String): ProfileDTO? {
        if (!email.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")))
            throw UnprocessableProfileException("Wrong email format")
        return profileService.getProfile(email)
            ?: throw ProfileNotFoundException("Profile with email '${email}' not found")
    }

    @PostMapping("/API/profiles")
    @ResponseStatus(HttpStatus.CREATED)
    fun addProfile(@RequestBody @Valid profile: ProfileDTO?, br: BindingResult) {
        if (br.hasErrors())
            throw UnprocessableProfileException("Wrong profile format")
        if (profile == null)
                throw BadRequestProfileException("Profile must not be NULL")
        if (profileService.getProfile(profile.email) != null)
            throw DuplicateProfileException("Profile with email '${profile.email}' already exists")
        profileService.addProfile(profile)
    }

    @PutMapping("/API/profiles/{email}")
    fun updateProfile(@PathVariable email: String, @RequestBody @Valid profile: ProfileDTO?, br: BindingResult) {
        if (!email.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")))
            throw UnprocessableProfileException("Wrong email format")
        if (br.hasErrors())
            throw UnprocessableProfileException("Wrong profile format")
        if (profile == null)
            throw BadRequestProfileException("Profile must not be NULL")
        if (profileService.getProfile(email) == null)
            throw ProfileNotFoundException("Profile with email '${email}' not found")
        profileService.updateProfile(email, profile)
    }
}