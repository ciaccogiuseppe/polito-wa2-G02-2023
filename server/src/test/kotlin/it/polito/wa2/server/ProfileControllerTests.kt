package it.polito.wa2.server

import dasniko.testcontainers.keycloak.KeycloakContainer
import it.polito.wa2.server.profiles.*
import jakarta.validation.constraints.NotBlank
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.json.BasicJsonParser
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
class ProfileControllerTests {
    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:latest")

        @Container
        val keycloak = KeycloakContainer().withRealmImportFile("keycloak/realm-test.json")

        var managerToken = ""
        var clientToken = ""
        var expertToken = ""


        @JvmStatic
        @BeforeAll
        fun setup(){
            keycloak.start()
            TestUtils.testKeycloakSetup(keycloak)
            managerToken = TestUtils.testKeycloakGetManagerToken(keycloak)
            clientToken = TestUtils.testKeycloakGetClientToken(keycloak)
            expertToken = TestUtils.testKeycloakGetExpertToken(keycloak)
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
    fun getExistingProfile() {


        val email = "mario.rossi@polito.it"
        val url = "http://localhost:$port/API/manager/profiles/$email"
        val uri = URI(url)
        val json = BasicJsonParser()

        val profile = ProfileDTO(
            "mario.rossi@polito.it",
            "mario",
            "rossi",
            null
        ).toNewProfile()

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(profile)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val entity = HttpEntity(null, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        val body = json.parseMap(result.body)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        Assertions.assertEquals(profile.email, body["email"])
        Assertions.assertEquals(profile.name, body["name"])
        Assertions.assertEquals(profile.surname, body["surname"])

        profileRepository.delete(profile)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getExistingProfileForbiddenClient() {


        val email = "mario.rossi@polito.it"
        val url = "http://localhost:$port/API/manager/profiles/$email"
        val uri = URI(url)
        val json = BasicJsonParser()

        val profile = ProfileDTO(
            "mario.rossi@polito.it",
            "mario",
            "rossi",
            null
        ).toNewProfile()

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(profile)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)

        val entity = HttpEntity(null, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        profileRepository.delete(profile)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getExistingProfileForbiddenExpert() {


        val email = "mario.rossi@polito.it"
        val url = "http://localhost:$port/API/manager/profiles/$email"
        val uri = URI(url)
        val json = BasicJsonParser()

        val profile = ProfileDTO(
            "mario.rossi@polito.it",
            "mario",
            "rossi",
            null
        ).toNewProfile()

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(profile)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(expertToken)

        val entity = HttpEntity(null, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        profileRepository.delete(profile)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getNonExistingProfile() {
        val email = "mario.bianchi@polito.it"
        val url = "http://localhost:$port/API/manager/profiles/$email"
        val uri = URI(url)

        val profile = ProfileDTO(
            "mario.rossi@polito.it",
            "mario",
            "rossi",
            null
        ).toNewProfile()

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(profile)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val entity = HttpEntity(null, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

        profileRepository.delete(profile)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getProfileWrongFormats() {
        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        val email1 = "@polito.it"
        val email2 = "abcpolito.it"
        val email3 = "abc@polito"
        val email4 = "abc@polito.i"

        val url1 = "http://localhost:$port/API/manager/profiles/$email1"
        val url2 = "http://localhost:$port/API/manager/profiles/$email2"
        val url3 = "http://localhost:$port/API/manager/profiles/$email3"
        val url4 = "http://localhost:$port/API/manager/profiles/$email4"

        val uri1 = URI(url1)
        val uri2 = URI(url2)
        val uri3 = URI(url3)
        val uri4 = URI(url4)


        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val entity = HttpEntity(null, headers)

        val result1 = restTemplate.exchange(
            uri1,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        val result2 = restTemplate.exchange(
            uri2,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        val result3 = restTemplate.exchange(
            uri3,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        val result4 = restTemplate.exchange(
            uri4,
            HttpMethod.GET,
            entity,
            String::class.java
        )


        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result1.statusCode)
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result2.statusCode)
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result3.statusCode)
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result4.statusCode)

        profileRepository.delete(manager)

    }

    @Test
    @DirtiesContext
    fun postProfileSuccess() {
        val url = "http://localhost:$port/API/public/profiles"
        val uri = URI(url)

        val profile = ProfileDTO(
            "mario.rossi@polito.it",
            "mario",
            "rossi",
            null
        )

        val requestEntity : HttpEntity<ProfileDTO> = HttpEntity(profile)
        val result = restTemplate.postForEntity(uri, requestEntity, profile.javaClass)

        Assertions.assertEquals(HttpStatus.CREATED, result.statusCode)

        val createdProfile = profileRepository.findByEmail(profile.email)

        Assertions.assertNotNull(createdProfile)
        Assertions.assertEquals(profile.email, createdProfile?.email)
        Assertions.assertEquals(profile.name, createdProfile?.name)
        Assertions.assertEquals(profile.surname, createdProfile?.surname)

        profileRepository.delete(createdProfile!!)
    }

    @Test
    @DirtiesContext
    fun postProfileRepeatedMail() {
        val url = "http://localhost:$port/API/public/profiles"
        val uri = URI(url)

        val profile = ProfileDTO(
            "mario.rossi@polito.it",
            "mario",
            "rossi",
            null
        )

        val requestEntity : HttpEntity<ProfileDTO> = HttpEntity(profile)
        restTemplate.postForEntity(uri, requestEntity, profile.javaClass)
        val result = restTemplate.postForEntity(uri, requestEntity, String.Companion::class.java)

        Assertions.assertEquals(HttpStatus.CONFLICT, result.statusCode)

        val createdProfile = profileRepository.findByEmail(profile.email)

        Assertions.assertNotNull(createdProfile)

        profileRepository.delete(createdProfile!!)
    }

    @Test
    @DirtiesContext
    fun postProfileWrongFormat() {
        val url = "http://localhost:$port/API/public/profiles"
        val uri = URI(url)

        val profile1 = ProfileDTO(
            "mario.rossipolito.it",
            "mario",
            "rossi",
            null
        )
        val profile2 = ProfileDTO(
            "mario.rossi@polito",
            "mario",
            "rossi",
            null
        )
        val profile3 = ProfileDTO(
            "mario.rossi@polito.i",
            "mario",
            "rossi",
            null
        )
        val profile4 = ProfileDTO(
            "mario.rossi@polito.",
            "mario",
            "rossi",
            null
        )

        val requestEntity1 : HttpEntity<ProfileDTO> = HttpEntity(profile1)
        val requestEntity2 : HttpEntity<ProfileDTO> = HttpEntity(profile2)
        val requestEntity3 : HttpEntity<ProfileDTO> = HttpEntity(profile3)
        val requestEntity4 : HttpEntity<ProfileDTO> = HttpEntity(profile4)

        val result1 = restTemplate.postForEntity(uri, requestEntity1, String.Companion::class.java)
        val result2 = restTemplate.postForEntity(uri, requestEntity2, String.Companion::class.java)
        val result3 = restTemplate.postForEntity(uri, requestEntity3, String.Companion::class.java)
        val result4 = restTemplate.postForEntity(uri, requestEntity4, String.Companion::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result1.statusCode)
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result2.statusCode)
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result3.statusCode)
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result4.statusCode)

        val createdProfile1 = profileRepository.findByEmail(profile1.email)
        val createdProfile2 = profileRepository.findByEmail(profile2.email)
        val createdProfile3 = profileRepository.findByEmail(profile3.email)
        val createdProfile4 = profileRepository.findByEmail(profile4.email)

        Assertions.assertNull(createdProfile1)
        Assertions.assertNull(createdProfile2)
        Assertions.assertNull(createdProfile3)
        Assertions.assertNull(createdProfile4)
    }

    @Test
    @DirtiesContext
    fun postProfileMissingFields() {
        val url = "http://localhost:$port/API/public/profiles"
        val uri = URI(url)

        data class WrongProfileDTO(
            val email: Any?,
            @field:NotBlank(message="name is mandatory")
            val name: String?,
            @field:NotBlank(message="surnname is mandatory")
            val surname: String?
        )

        data class NoFieldsProfileDTO(
            @field:NotBlank(message="name is mandatory")
            val name: String?,
            @field:NotBlank(message="surnname is mandatory")
            val surname: String?
        )

        val profile1 = WrongProfileDTO(
            12,
            "mario",
            "rossi"
        )
        val profile2 = WrongProfileDTO(
            12,
            null,
            "rossi"
        )
        val profile3 = NoFieldsProfileDTO(
            "mario",
            "rossi"
        )


        val requestEntity1 : HttpEntity<WrongProfileDTO> = HttpEntity(profile1)
        val requestEntity2 : HttpEntity<WrongProfileDTO> = HttpEntity(profile2)
        val requestEntity3 : HttpEntity<NoFieldsProfileDTO> = HttpEntity(profile3)


        val result1 = restTemplate.postForEntity(uri, requestEntity1, String.Companion::class.java)
        val result2 = restTemplate.postForEntity(uri, requestEntity2, String.Companion::class.java)
        val result3 = restTemplate.postForEntity(uri, requestEntity3, String.Companion::class.java)


        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result1.statusCode)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result2.statusCode)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result3.statusCode)
    }

    @Test
    @DirtiesContext
    fun putProfileSuccessClient() {
        val email = "client@polito.it"
        val url = "http://localhost:$port/API/client/profiles/$email"
        val uri = URI(url)

        val profile = ProfileDTO(
            "client@polito.it",
            "mario",
            "rossi",
            null
        ).toNewProfile()

        val newProfile = ProfileDTO(
            "client@polito.it",
            "Mario",
            "Bianchi",
            null
        )

        profileRepository.save(profile)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)


        val requestEntity : HttpEntity<ProfileDTO> = HttpEntity(newProfile, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedProfile = profileRepository.findByEmail(newProfile.email)

        Assertions.assertNotNull(updatedProfile)
        Assertions.assertEquals(newProfile.email, updatedProfile?.email)
        Assertions.assertEquals(newProfile.name, updatedProfile?.name)
        Assertions.assertEquals(newProfile.surname, updatedProfile?.surname)

        profileRepository.delete(updatedProfile!!)
    }

    @Test
    @DirtiesContext
    fun putProfileForbiddenClientEmail() {
        val email = "client2@polito.it"
        val url = "http://localhost:$port/API/client/profiles/$email"
        val uri = URI(url)

        val profile = ProfileDTO(
            "client@polito.it",
            "mario",
            "rossi",
            null
        ).toNewProfile()

        val newProfile = ProfileDTO(
            "client@polito.it",
            "Mario",
            "Bianchi",
            null
        )

        profileRepository.save(profile)


        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)


        val requestEntity : HttpEntity<ProfileDTO> = HttpEntity(newProfile, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)
        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        profileRepository.delete(profile)
    }

    /*@Test
    @DirtiesContext
    fun putProfileWrongFormat() {
        val email = "client@polito.it"
        val url = "http://localhost:$port/API/profiles/$email"
        val uri = URI(url)

        val profile = ProfileDTO(
            "client@polito.it",
            "mario",
            "rossi",
            null,
        ).toNewProfile()


        val newProfile = ProfileDTO(
            "client@polimi.it",
            "Mario",
            "Bianchi",
            null
        )

        profileRepository.save(profile)


        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)


        val requestEntity : HttpEntity<ProfileDTO> = HttpEntity(newProfile, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        profileRepository.delete(profile)
    }*/
}

