package it.polito.wa2.server

import dasniko.testcontainers.keycloak.KeycloakContainer
import it.polito.wa2.server.addresses.AddressDTO
import it.polito.wa2.server.keycloak.UserDTO
import it.polito.wa2.server.keycloak.UserUpdateDTO
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileRole
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class KeycloakControllerTests {

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
        fun setup() {
            keycloak.start()
            TestUtils.testKeycloakSetup(keycloak)
            managerToken = TestUtils.testKeycloakGetManagerToken(keycloak)
            clientToken = TestUtils.testKeycloakGetClientToken(keycloak)
            expertToken = TestUtils.testKeycloakGetExpertToken(keycloak)
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
    lateinit var profileRepository: ProfileRepository

    /*@Test
    //@DirtiesContext
    fun postClient() {
        val uri = URI("http://localhost:$port/API/signup")

        val address = AddressDTO(
            "Italy",
            "Piemonte",
            "Torino",
            "Corso Duca degli Abruzzi, 24"
        )

        val user = UserDTO(
            "MarioR_99",
            "ticketing.wa2g02@gmail.com",
            "password",
            "Mario",
            "Rossi",
            setOf(),
            address,
            "CLIENT"
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val requestEntity: HttpEntity<UserDTO> = HttpEntity(user, headers)
        val result = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.CREATED, result.statusCode)

        val createdProfile = profileRepository.findByEmail(user.email)
        profileRepository.delete(createdProfile!!)
        Assertions.assertNotNull(createdProfile)
        Assertions.assertEquals(user.email, createdProfile.email)
        Assertions.assertEquals(user.firstName, createdProfile.name)
        Assertions.assertEquals(user.lastName, createdProfile.surname)


        val createdUser = TestUtils.testKeycloakGetUser(keycloak, user.email)
        Assertions.assertNotNull(createdUser)
        Assertions.assertEquals(user.email, createdUser?.email)
        Assertions.assertEquals(user.firstName, createdUser?.firstName)
        Assertions.assertEquals(user.lastName, createdUser?.lastName)


    }*/

    @Test
    //@DirtiesContext
    fun putClient() {
        val uri = URI("http://localhost:$port/API/client/user/client@polito.it")

        val address = AddressDTO(
            "Italy",
            "Piemonte",
            "Torino",
            "Corso Duca degli Abruzzi, 24"
        )

        val user = UserDTO(
            "MarioR_99",
            "client@polito.it",
            "password",
            "Mario",
            "Bianchi",
            setOf(),
            address,
            "CLIENT"
        )
        val profile = TestUtils.testProfile("client@polito.it", "Profile", "Polito", ProfileRole.CLIENT)
        profileRepository.save(profile)


        val entity = TestUtils.testEntityHeader(user, clientToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val createdProfile = profileRepository.findByEmail(user.email)

        Assertions.assertNotNull(createdProfile)
        profileRepository.delete(createdProfile!!)
        Assertions.assertEquals(user.email, createdProfile.email)
        Assertions.assertEquals(user.firstName, createdProfile.name)
        Assertions.assertEquals(user.lastName, createdProfile.surname)


        val createdUser = TestUtils.testKeycloakGetUser(keycloak, user.email)
        Assertions.assertNotNull(createdUser)
        Assertions.assertEquals(user.email, createdUser?.email)
        Assertions.assertEquals(user.firstName, createdUser?.firstName)
        Assertions.assertEquals(user.lastName, createdUser?.lastName)


    }

    @Test
    //@DirtiesContext
    fun putManager() {
        val uri = URI("http://localhost:$port/API/manager/user/manager@polito.it")

        val user = UserUpdateDTO(
            "manager@polito.it",
            "Mario",
            "Bianchi",
            setOf(),
            null,
            "MANAGER"
        )
        val profile = TestUtils.testProfile("manager@polito.it", "Profile", "Polito", ProfileRole.MANAGER)
        profileRepository.save(profile)


        val entity = TestUtils.testEntityHeader(user, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val createdProfile = profileRepository.findByEmail(user.email)

        Assertions.assertNotNull(createdProfile)
        profileRepository.delete(createdProfile!!)
        Assertions.assertEquals(user.email, createdProfile.email)
        Assertions.assertEquals(user.firstName, createdProfile.name)
        Assertions.assertEquals(user.lastName, createdProfile.surname)


        val createdUser = TestUtils.testKeycloakGetUser(keycloak, user.email)
        Assertions.assertNotNull(createdUser)
        Assertions.assertEquals(user.email, createdUser?.email)
        Assertions.assertEquals(user.firstName, createdUser?.firstName)
        Assertions.assertEquals(user.lastName, createdUser?.lastName)


    }

    @Test
    //@DirtiesContext
    fun putExpert() {
        val uri = URI("http://localhost:$port/API/expert/user/expert@polito.it")

        val user = UserUpdateDTO(
            "expert@polito.it",
            "Mario",
            "Bianchi",
            setOf(),
            null,
            "EXPERT"
        )
        val profile = TestUtils.testProfile("expert@polito.it", "Profile", "Polito", ProfileRole.EXPERT)
        profileRepository.save(profile)


        val entity = TestUtils.testEntityHeader(user, expertToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val createdProfile = profileRepository.findByEmail(user.email)

        Assertions.assertNotNull(createdProfile)
        profileRepository.delete(createdProfile!!)
        Assertions.assertEquals(user.email, createdProfile.email)
        Assertions.assertEquals(user.firstName, createdProfile.name)
        Assertions.assertEquals(user.lastName, createdProfile.surname)


        val createdUser = TestUtils.testKeycloakGetUser(keycloak, user.email)
        Assertions.assertNotNull(createdUser)
        Assertions.assertEquals(user.email, createdUser?.email)
        Assertions.assertEquals(user.firstName, createdUser?.firstName)
        Assertions.assertEquals(user.lastName, createdUser?.lastName)


    }

    @Test
    //@DirtiesContext
    fun postClientBadRequest() {
        val uri = URI("http://localhost:$port/API/signup")

        val user: UserDTO? = null

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val requestEntity: HttpEntity<UserDTO> = HttpEntity(user, headers)
        val result = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }

    @Test
    //@DirtiesContext
    fun postClientDuplicate() {
        val uri = URI("http://localhost:$port/API/signup")

        val profile = TestUtils.testProfile("mario.rossi@polito.it", "Profile", "Polito", ProfileRole.CLIENT)
        profileRepository.save(profile)

        val address = AddressDTO(
            "Italy",
            "Piemonte",
            "Torino",
            "Corso Duca degli Abruzzi, 24"
        )

        val user = UserDTO(
            "MarioR_99",
            "mario.rossi@polito.it",
            "password",
            "Mario",
            "Rossi",
            setOf(),
            address,
            "CLIENT"
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val requestEntity: HttpEntity<UserDTO> = HttpEntity(user, headers)
        val result = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String::class.java)

        profileRepository.delete(profile)
        Assertions.assertEquals(HttpStatus.CONFLICT, result.statusCode)


    }

    /*@Test
    //@DirtiesContext
    fun postExpert() {
        val uri = URI("http://localhost:$port/API/createExpert")

        val user = UserDTO(
            "LuigiV_99",
            "ticketing.wa2g02@gmail.com",
            "password",
            "Luigi",
            "Verdi",
            setOf(),
            null,
            "EXPERT"
        )

        val entity = TestUtils.testEntityHeader(user, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.POST, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.CREATED, result.statusCode)

        val createdProfile = profileRepository.findByEmail(user.email)

        Assertions.assertNotNull(createdProfile)
        profileRepository.delete(createdProfile!!)
        Assertions.assertEquals(user.email, createdProfile.email)
        Assertions.assertEquals(user.firstName, createdProfile.name)
        Assertions.assertEquals(user.lastName, createdProfile.surname)


        val createdUser = TestUtils.testKeycloakGetUser(keycloak, user.email)
        Assertions.assertNotNull(createdUser)
        Assertions.assertEquals(user.email, createdUser?.email)
        Assertions.assertEquals(user.firstName, createdUser?.firstName)
        Assertions.assertEquals(user.lastName, createdUser?.lastName)


    }*/

    /*@Test
    //@DirtiesContext
    fun postVendor() {
        val uri = URI("http://localhost:$port/API/createVendor")

        val user = UserDTO(
            "LuigiV_99",
            "ticketing.wa2g02@gmail.com",
            "password",
            "Luigi",
            "Verdi",
            setOf(),
            null,
            "VENDOR"
        )

        val entity = TestUtils.testEntityHeader(user, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.POST, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.CREATED, result.statusCode)

        val createdProfile = profileRepository.findByEmail(user.email)

        Assertions.assertNotNull(createdProfile)
        profileRepository.delete(createdProfile!!)
        Assertions.assertEquals(user.email, createdProfile.email)
        Assertions.assertEquals(user.firstName, createdProfile.name)
        Assertions.assertEquals(user.lastName, createdProfile.surname)


        val createdUser = TestUtils.testKeycloakGetUser(keycloak, user.email)
        Assertions.assertNotNull(createdUser)
        Assertions.assertEquals(user.email, createdUser?.email)
        Assertions.assertEquals(user.firstName, createdUser?.firstName)
        Assertions.assertEquals(user.lastName, createdUser?.lastName)


    }*/

    @Test
    //@DirtiesContext
    fun postExpertBadRequest() {
        val uri = URI("http://localhost:$port/API/createExpert")

        val user: UserDTO? = null

        val entity = TestUtils.testEntityHeader(user, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.POST, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }

    @Test
    //@DirtiesContext
    fun postExpertDuplicate() {
        val uri = URI("http://localhost:$port/API/createExpert")

        val profile = TestUtils.testProfile("luigi.verdi@polito.it", "Profile", "Polito", ProfileRole.EXPERT)
        profileRepository.save(profile)

        val user = UserDTO(
            "LuigiV_99",
            "luigi.verdi@polito.it",
            "password",
            "Luigi",
            "Verdi",
            setOf(),
            null,
            "EXPERT"
        )

        val entity = TestUtils.testEntityHeader(user, managerToken)

        val result = restTemplate.exchange(uri, HttpMethod.POST, entity, String::class.java)
        profileRepository.delete(profile)
        Assertions.assertEquals(HttpStatus.CONFLICT, result.statusCode)


    }

    @Test
    //@DirtiesContext
    fun postVendorDuplicate() {
        val uri = URI("http://localhost:$port/API/createVendor")

        val profile = TestUtils.testProfile("luigi.verdi@polito.it", "Profile", "Polito", ProfileRole.VENDOR)
        profileRepository.save(profile)

        val user = UserDTO(
            "LuigiV_99",
            "luigi.verdi@polito.it",
            "password",
            "Luigi",
            "Verdi",
            setOf(),
            null,
            "VENDOR"
        )

        val entity = TestUtils.testEntityHeader(user, managerToken)

        val result = restTemplate.exchange(uri, HttpMethod.POST, entity, String::class.java)
        profileRepository.delete(profile)
        Assertions.assertEquals(HttpStatus.CONFLICT, result.statusCode)


    }

}
