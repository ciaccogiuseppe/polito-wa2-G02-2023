package it.polito.wa2.server.security

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.LoginFailedException
import it.polito.wa2.server.UnprocessableProfileException
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.json.BasicJsonParser
import org.springframework.http.*
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.net.http.HttpClient


@CrossOrigin(origins = ["http://localhost:3001"])
@RestController
@Observed
class AuthController(
    @Value("\${keycloak.auth-server-url}") private val authServerUrl: String,
    @Value("\${keycloak.realm}") private val realm: String,
    @Value("\${keycloak.resource}") private val clientId: String
) {
    private val restTemplate = RestTemplate()

    @PostMapping("/API/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest, br: BindingResult): LoginResponse {
        checkInputParameters(br)
        val headers = HttpHeaders()
        headers.set("Content-Type", "application/x-www-form-urlencoded")
        val body = "grant_type=password&username=${loginRequest.username}&password=${loginRequest.password}" +
                "&client_id=$clientId" + "&scope=openid offline_access"

        val entity = HttpEntity(body, headers)

        val tokenUrl = "$authServerUrl/realms/$realm/protocol/openid-connect/token"
        return sendRequest(entity, tokenUrl)
    }

    @PostMapping("/API/refreshToken")
    fun refreshToken(@Valid @RequestBody refreshRequest: RefreshRequest, br: BindingResult): LoginResponse {
        checkInputParameters(br)
        val headers = HttpHeaders()
        headers.set("Content-Type", "application/x-www-form-urlencoded")
        val body = "grant_type=refresh_token&refresh_token=${refreshRequest.refreshToken}&client_id=$clientId"

        val entity = HttpEntity(body, headers)

        val tokenUrl = "$authServerUrl/realms/$realm/protocol/openid-connect/token"
        println(tokenUrl)
        println(body)
        return sendRequest(entity, tokenUrl)
    }

    private fun sendRequest(entity: HttpEntity<String>, tokenUrl: String): LoginResponse {
        val json = BasicJsonParser()
        try {
            val response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                entity,
                String::class.java
            )
            println(response)
            val responseBody = response.body ?: throw IllegalStateException("Unable to retrieve access token")
            val tokenResponse = TokenResponse(
                json.parseMap(responseBody)["refresh_token"]!!.toString(),
                json.parseMap(responseBody)["access_token"]!!.toString(),
                json.parseMap(responseBody)["expires_in"]!! as Long
            )
            return LoginResponse(tokenResponse.refreshToken, tokenResponse.token, tokenResponse.expiresIn)

        }
        catch (e : HttpClientErrorException) {
            if(e.message!!.contains("Account is not fully set up"))
                throw LoginFailedException("EMAIL_NOT_ACTIVATED")
            else
                throw LoginFailedException("Login failed")
        }

        catch (e: RuntimeException) {
            throw LoginFailedException("Login failed")
        }

    }

    private fun checkInputParameters(br: BindingResult) {
        if (br.hasErrors())
            throw UnprocessableProfileException("Wrong format for authentication request")
    }
}

data class LoginRequest(@field:NotBlank val username: String, @field:NotBlank val password: String)

data class RefreshRequest(@field:NotBlank val refreshToken: String)
data class LoginResponse(
    @field:NotBlank val refreshToken: String,
    @field:NotBlank val token: String,
    @field:Positive val expiresIn: Long
)

data class TokenResponse(
    @field:NotBlank val refreshToken: String,
    @field:NotBlank val token: String,
    @field:Positive val expiresIn: Long
)