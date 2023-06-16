package it.polito.wa2.server.security

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.LoginFailedException
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.json.BasicJsonParser
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate


@RestController
@Observed
class AuthController(
    @Value("\${keycloak.auth-server-url}") private val authServerUrl: String,
    @Value("\${keycloak.realm}") private val realm: String,
    @Value("\${keycloak.resource}") private val clientId: String
) {
    private val restTemplate = RestTemplate()

    @CrossOrigin(origins =["http://localhost:3001"])
    @PostMapping("/API/login")
    @ResponseBody
    fun login(@RequestBody loginRequest: LoginRequest): LoginResponse {
        val headers = HttpHeaders()
        headers.set("Content-Type", "application/x-www-form-urlencoded")
        val body = "grant_type=password&username=${loginRequest.username}&password=${loginRequest.password}" +
                "&client_id=$clientId"

        val entity = HttpEntity(body, headers)

        val tokenUrl = "$authServerUrl/realms/$realm/protocol/openid-connect/token"
        println(tokenUrl)
        println(body)
        val json = BasicJsonParser()
        try {
            val response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                entity,
                String::class.java)
            println(response)
            val responseBody = response.body ?: throw IllegalStateException("Unable to retrieve access token")
            val tokenResponse = TokenResponse(
                json.parseMap(responseBody)["refresh_token"]!!.toString(),
                json.parseMap(responseBody)["access_token"]!!.toString(),
                json.parseMap(responseBody)["expires_in"]!! as Long
            )
            return LoginResponse(tokenResponse.refreshToken, tokenResponse.token, tokenResponse.expiresIn)

        } catch(e: RuntimeException){
            println(e)
            throw LoginFailedException("Login failed")
        }
    }

    @CrossOrigin(origins =["http://localhost:3001"])
    @PostMapping("/API/refreshtoken")
    @ResponseBody
    fun refreshToken(@RequestBody refreshRequest: RefreshRequest): LoginResponse {
        val headers = HttpHeaders()
        headers.set("Content-Type", "application/x-www-form-urlencoded")
        val body = "grant_type=refresh_token&refresh_token=${refreshRequest.refreshToken}&client_id=$clientId"

        val entity = HttpEntity(body, headers)

        val tokenUrl = "$authServerUrl/realms/$realm/protocol/openid-connect/token"
        val json = BasicJsonParser()
        try {
            val response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                entity,
                String::class.java)
            println(response)
            val responseBody = response.body ?: throw IllegalStateException("Unable to retrieve access token")
            val tokenResponse = TokenResponse(
                json.parseMap(responseBody)["refresh_token"]!!.toString(),
                json.parseMap(responseBody)["access_token"]!!.toString(),
                json.parseMap(responseBody)["expires_in"]!! as Long
            )
            return LoginResponse(tokenResponse.refreshToken, tokenResponse.token, tokenResponse.expiresIn)

        } catch(e: RuntimeException){
            println(e)
            throw LoginFailedException("Login failed")
        }
    }



}

data class LoginRequest(val username: String, val password: String)

data class RefreshRequest(val refreshToken: String)
data class LoginResponse(val refreshToken: String, val token: String, val expiresIn: Long)
data class TokenResponse(val refreshToken: String, val token: String, val expiresIn:Long)