package it.polito.wa2.server

import dasniko.testcontainers.keycloak.KeycloakContainer
import it.polito.wa2.server.security.LoginRequest
import it.polito.wa2.server.security.RefreshRequest
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.json.BasicJsonParser
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.web.client.ResponseErrorHandler
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthControllerTest {

    val json = BasicJsonParser()

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:latest")

        @Container
        val keycloak = KeycloakContainer().withRealmImportFile("keycloak/realm-test.json")


        @JvmStatic
        @BeforeAll
        fun setup() {
            keycloak.start()
            TestUtils.testKeycloakSetup(keycloak)
        }

        @JvmStatic
        @AfterAll
        fun clean() {
            keycloak.stop()
            postgres.close()
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
            registry.add("spring.datasource.hikari.validation-timeout") { "250" }
            registry.add("spring.datasource.hikari.connection-timeout") { "250" }
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri")
            { keycloak.authServerUrl + "realms/SpringBootKeycloak" }
            registry.add("keycloak.auth-server-url") { keycloak.authServerUrl }
            registry.add("keycloak.realm") { "SpringBootKeycloak" }
            registry.add("keycloak.resource") { "springboot-keycloak-client" }
            registry.add("keycloak.credentials.secret") {
                keycloak.keycloakAdminClient.realm("SpringBootKeycloak").clients()
                    .findByClientId("springboot-keycloak-client")[0].secret
            }
        }
    }

    @LocalServerPort
    protected var port: Int = 8080

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var restTemplateE: TestRestTemplate


    @Test
    //@DirtiesContext
    fun loginManagerSuccessful() {
        val url = "http://localhost:$port/API/login"
        val uri = URI(url)


        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val loginRequest = LoginRequest("manager@polito.it", "password")

        val requestEntity: HttpEntity<LoginRequest> = HttpEntity(loginRequest, headers)
        val result = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
    }

    @Test
    //@DirtiesContext
    fun loginManagerWrongPassword() {
        val url = "http://localhost:$port/API/login"
        val uri = URI(url)

        val reqFactory = SimpleClientHttpRequestFactory()
        reqFactory.setOutputStreaming(false)

        restTemplateE.restTemplate.requestFactory = reqFactory
        restTemplateE.restTemplate.errorHandler = object : ResponseErrorHandler {
            override fun hasError(response: ClientHttpResponse): Boolean {
                return response.statusCode.isError
            }

            override fun handleError(response: ClientHttpResponse) {
                if (response.statusCode.is4xxClientError) {
                    if (response.statusCode.value() == 401) {
                        println("unauthorized")
                    }
                }
            }
        }

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val loginRequest = LoginRequest("manager@polito.it", "password1")

        val requestEntity: HttpEntity<LoginRequest> = HttpEntity(loginRequest, headers)
        val result = restTemplateE.exchange(uri, HttpMethod.POST, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)
    }

    @Test
    //@DirtiesContext
    fun loginClientSuccessful() {
        val url = "http://localhost:$port/API/login"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val loginRequest = LoginRequest("client@polito.it", "password")

        val requestEntity: HttpEntity<LoginRequest> = HttpEntity(loginRequest, headers)
        val result = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
    }

    @Test
    //@DirtiesContext
    fun loginClientWrongPassword() {
        val url = "http://localhost:$port/API/login"
        val uri = URI(url)

        val reqFactory = SimpleClientHttpRequestFactory()
        reqFactory.setOutputStreaming(false)

        restTemplateE.restTemplate.requestFactory = reqFactory
        restTemplateE.restTemplate.errorHandler = object : ResponseErrorHandler {
            override fun hasError(response: ClientHttpResponse): Boolean {
                return response.statusCode.isError
            }

            override fun handleError(response: ClientHttpResponse) {
                if (response.statusCode.is4xxClientError) {
                    if (response.statusCode.value() == 401) {
                        println("unauthorized")
                    }
                }
            }
        }


        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val loginRequest = LoginRequest("client@polito.it", "password1")

        val requestEntity: HttpEntity<LoginRequest> = HttpEntity(loginRequest, headers)

        val result = restTemplateE.exchange(uri, HttpMethod.POST, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)
    }

    @Test
    //@DirtiesContext
    fun loginExpertSuccessful() {
        val url = "http://localhost:$port/API/login"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val loginRequest = LoginRequest("expert@polito.it", "password")

        val requestEntity: HttpEntity<LoginRequest> = HttpEntity(loginRequest, headers)
        val result = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
    }

    @Test
    //@DirtiesContext
    fun loginExpertWrongPassword() {
        val url = "http://localhost:$port/API/login"
        val uri = URI(url)

        val reqFactory = SimpleClientHttpRequestFactory()
        reqFactory.setOutputStreaming(false)

        restTemplateE.restTemplate.requestFactory = reqFactory
        restTemplateE.restTemplate.errorHandler = object : ResponseErrorHandler {
            override fun hasError(response: ClientHttpResponse): Boolean {
                return response.statusCode.isError
            }

            override fun handleError(response: ClientHttpResponse) {
                if (response.statusCode.is4xxClientError) {
                    if (response.statusCode.value() == 401) {
                        println("unauthorized")
                    }
                }
            }
        }

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val loginRequest = LoginRequest("expert@polito.it", "password1")

        val requestEntity: HttpEntity<LoginRequest> = HttpEntity(loginRequest, headers)
        val result = restTemplateE.exchange(uri, HttpMethod.POST, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)
    }

    @Test
    fun loginClientRefreshTokenSuccessful() {
        val url = "http://localhost:$port/API/login"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val loginRequest = LoginRequest("client@polito.it", "password")

        val requestEntity: HttpEntity<LoginRequest> = HttpEntity(loginRequest, headers)
        val result = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val url2 = "http://localhost:$port/API/refreshToken"
        val uri2 = URI(url2)

        val refreshToken = json.parseMap(result.body)["refreshToken"] as String

        val headers2 = HttpHeaders()
        headers2.contentType = MediaType.APPLICATION_JSON
        val refreshRequest = RefreshRequest(refreshToken)

        val requestEntity2: HttpEntity<RefreshRequest> = HttpEntity(refreshRequest, headers2)
        val result2 = restTemplate.exchange(uri2, HttpMethod.POST, requestEntity2, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result2.statusCode)
    }

    @Test
    fun loginClientRefreshTokenFailed() {
        val url = "http://localhost:$port/API/refreshToken"
        val uri = URI(url)

        val refreshToken = "wrongToken"

        val reqFactory = SimpleClientHttpRequestFactory()
        reqFactory.setOutputStreaming(false)

        restTemplateE.restTemplate.requestFactory = reqFactory
        restTemplateE.restTemplate.errorHandler = object : ResponseErrorHandler {
            override fun hasError(response: ClientHttpResponse): Boolean {
                return response.statusCode.isError
            }

            override fun handleError(response: ClientHttpResponse) {
                if (response.statusCode.is4xxClientError) {
                    if (response.statusCode.value() == 401) {
                        println("unauthorized")
                    }
                }
            }
        }

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val refreshRequest = RefreshRequest(refreshToken)

        val requestEntity: HttpEntity<RefreshRequest> = HttpEntity(refreshRequest, headers)
        val result = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)
    }
}

