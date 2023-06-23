package it.polito.wa2.server

import dasniko.testcontainers.keycloak.KeycloakContainer
import it.polito.wa2.server.categories.Category
import it.polito.wa2.server.categories.CategoryRepository
import it.polito.wa2.server.categories.ProductCategory
import it.polito.wa2.server.profiles.*
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
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URI
import java.util.LinkedHashMap


@Testcontainers
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
class ProfileControllerTests {
    val json = BasicJsonParser()
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
        @AfterAll
        fun clean(){
            keycloak.stop()
            postgres.close()
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") {"create-drop"}
            registry.add("spring.datasource.hikari.validation-timeout"){"250"}
            registry.add("spring.datasource.hikari.connection-timeout"){"250"}
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
    @Autowired
    lateinit var categoryRepository: CategoryRepository
    @Test
        //@DirtiesContext
    fun getSelfProfile() {
        val email = "mario.rossi@polito.it"
        val uri = URI("http://localhost:$port/API/authenticated/profile/")

        val profile = TestUtils.testProfile(email, "Profile", "Polito", ProfileRole.CLIENT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(profile)

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )


        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseMap(result.body)
        Assertions.assertEquals(manager.email, body["email"])
        Assertions.assertEquals(manager.name, body["name"])
        Assertions.assertEquals(manager.surname, body["surname"])

        profileRepository.delete(profile)
        profileRepository.delete(manager)
    }

    @Test
        //@DirtiesContext
    fun getExistingProfile() {
        val email = "mario.rossi@polito.it"
        val uri = URI("http://localhost:$port/API/authenticated/profiles/$email")

        val profile = TestUtils.testProfile(email, "Profile", "Polito", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        val client = TestUtils.testProfile("client@polito.it", "Client", "PoliTo", ProfileRole.CLIENT)
        profileRepository.save(manager)
        profileRepository.save(client)
        profileRepository.save(profile)

        val entity = TestUtils.testEntityHeader(null, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )


        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseMap(result.body)
        Assertions.assertEquals(profile.email, body["email"])
        Assertions.assertEquals(profile.name, body["name"])
        Assertions.assertEquals(profile.surname, body["surname"])

        profileRepository.delete(profile)
        profileRepository.delete(manager)
        profileRepository.delete(client)
    }
@Test
        //@DirtiesContext
    fun getNotFoundProfile() {
        val email = "mariorossi@polito.it"
        val email2 = "mario.rossi@polito.it"
        val uri = URI("http://localhost:$port/API/authenticated/profiles/$email2")

        val profile = TestUtils.testProfile(email, "Profile", "Polito", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(profile)

        val entity = TestUtils.testEntityHeader(null, clientToken)

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
        //@DirtiesContext
    fun getExpertsByCategory() {
        val email = "mariorossi@polito.it"
        val email2 = "mario.rossi@polito.it"
        val uri = URI("http://localhost:$port/API/manager/profiles/experts/PC")

        val category1 = Category().apply{ name = ProductCategory.PC }
        val category2 = Category().apply { name = ProductCategory.TV }

        categoryRepository.save(category1)
        categoryRepository.save(category2)

        val profile = TestUtils.testProfile(email, "Profile", "Polito", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        val expert1 = TestUtils.testProfile("e1@polito.it", "Manager", "Polito", ProfileRole.EXPERT)
        val expert2 = TestUtils.testProfile("e2@polito.it", "Manager", "Polito", ProfileRole.EXPERT)
        val expert3 = TestUtils.testProfile("e3@polito.it", "Manager", "Polito", ProfileRole.EXPERT)
        expert1.expertCategories=mutableSetOf(category1, category2)
        expert2.expertCategories=mutableSetOf(category1)
        expert3.expertCategories=mutableSetOf(category2)
        profileRepository.save(manager)
        profileRepository.save(profile)
        profileRepository.save(expert1)
        profileRepository.save(expert2)
        profileRepository.save(expert3)

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )


        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*, *> }
        Assertions.assertEquals(2, body.size)

        profileRepository.delete(profile)
        profileRepository.delete(expert1)
        profileRepository.delete(expert2)
        profileRepository.delete(expert3)
        categoryRepository.delete(category1)
        categoryRepository.delete(category2)
        profileRepository.delete(manager)
    }


}

