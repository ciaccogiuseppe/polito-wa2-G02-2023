package it.polito.wa2.server.profiles

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.UnprocessableProfileException
import it.polito.wa2.server.categories.ProductCategory
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*
import java.security.Principal

@CrossOrigin(origins =["http://localhost:3001"])
@RestController
@Observed
class ProfileController(private val profileService: ProfileService) {

    @GetMapping("/API/authenticated/profile/")
    fun getProfileInfo(principal: Principal) : ProfileDTO{
        val userEmail = retrieveUserEmail(principal)
        return profileService.getProfileInfo(userEmail)
    }

    @GetMapping("/API/authenticated/profiles/{email}")
    fun getProfile(principal: Principal, @PathVariable email: String): ProfileDTO {
        val userEmail = retrieveUserEmail(principal)
        checkEmail(email)
        return profileService.getProfile(email, userEmail)
    }

    @GetMapping("/API/manager/profiles/experts/{category}")
    fun getExpertsByCategory(@PathVariable category: ProductCategory): List<ProfileDTO> {
        return profileService.getExpertByCategory(category)
    }

    private fun retrieveUserEmail(principal: Principal): String {
        val token: JwtAuthenticationToken = principal as JwtAuthenticationToken
        return token.tokenAttributes["email"] as String
    }

    private fun checkEmail(email: String){
        if (!email.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")))
            throw UnprocessableProfileException("Wrong email format")
    }

}