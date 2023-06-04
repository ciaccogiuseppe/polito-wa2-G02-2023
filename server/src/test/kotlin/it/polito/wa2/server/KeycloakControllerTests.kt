package it.polito.wa2.server

import dasniko.testcontainers.keycloak.KeycloakContainer
import it.polito.wa2.server.keycloak.UserDTO
import it.polito.wa2.server.profiles.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI

@Testcontainers
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
class KeycloakControllerTests {

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:latest")

        @Container
        val keycloak = KeycloakContainer().withRealmImportFile("keycloak/realm-test.json")


        @JvmStatic
        @BeforeAll
        fun setup(){
            keycloak.start()
            TestUtils.testKeycloakSetup(keycloak)
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") {"create-drop"}

            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri")
            { keycloak.authServerUrl + "realms/SpringBootKeycloak"}
            registry.add("keycloak.auth-server-url"){keycloak.authServerUrl}
            registry.add("keycloak.realm"){"SpringBootKeycloak"}
            registry.add("keycloak.resource"){"springboot-keycloak-client"}
            registry.add("keycloak.credentials.secret"){keycloak.keycloakAdminClient.realm("SpringBootKeycloak").clients().findByClientId("springboot-keycloak-client")[0].secret}
        }
    }

    @LocalServerPort
    protected var port: Int = 8080
    @Autowired
    lateinit var restTemplate: TestRestTemplate
    @Autowired
    lateinit var profileRepository: ProfileRepository

    @Test
    @DirtiesContext
    fun postClient() {
        val uri = URI("http://localhost:$port/API/signup")

        val user = UserDTO(
            "MarioR_99",
            "mario.rossi@polito.it",
            "password",
            "Mario",
            "Rossi"
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val requestEntity : HttpEntity<UserDTO> = HttpEntity(user, headers)
        val result = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.CREATED, result.statusCode)

        val createdProfile = profileRepository.findByEmail(user.email)

        Assertions.assertNotNull(createdProfile)
        Assertions.assertEquals(user.email, createdProfile?.email)
        Assertions.assertEquals(user.firstName, createdProfile?.name)
        Assertions.assertEquals(user.lastName, createdProfile?.surname)

        //TODO: check that user is also saved on Keycloak side (?)

        profileRepository.delete(createdProfile!!)
    }

    @Test
    @DirtiesContext
    fun postExpert() {
        val uri = URI("http://localhost:$port/API/createExpert")

        val user = UserDTO(
            "LuigiV_99",
            "luigi.verdi@polito.it",
            "password",
            "Luigi",
            "Verdi"
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val requestEntity : HttpEntity<UserDTO> = HttpEntity(user, headers)
        val result = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.CREATED, result.statusCode)

        val createdProfile = profileRepository.findByEmail(user.email)

        Assertions.assertNotNull(createdProfile)
        Assertions.assertEquals(user.email, createdProfile?.email)
        Assertions.assertEquals(user.firstName, createdProfile?.name)
        Assertions.assertEquals(user.lastName, createdProfile?.surname)

        //TODO: check that user is also saved on Keycloak side (?)

        profileRepository.delete(createdProfile!!)
    }

}
