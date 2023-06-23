package it.polito.wa2.server

import dasniko.testcontainers.keycloak.KeycloakContainer
import it.polito.wa2.server.brands.Brand
import it.polito.wa2.server.brands.BrandDTO
import it.polito.wa2.server.brands.BrandRepository
import it.polito.wa2.server.categories.Category
import it.polito.wa2.server.categories.CategoryRepository
import it.polito.wa2.server.categories.ProductCategory
import it.polito.wa2.server.products.ProductDTO
import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.products.toDTO
import it.polito.wa2.server.profiles.ProfileDTO
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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.*
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
class BrandControllerTests {
    val json = BasicJsonParser()
    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:latest")

        @Container
        val keycloak = KeycloakContainer().withRealmImportFile("keycloak/realm-test.json")

        var managerToken = ""
        var clientToken = ""
        var expertToken = ""
        var vendorToken = ""


        @JvmStatic
        @BeforeAll
        fun setup(){
            keycloak.start()
            TestUtils.testKeycloakSetup(keycloak)
            managerToken = TestUtils.testKeycloakGetManagerToken(keycloak)
            clientToken = TestUtils.testKeycloakGetClientToken(keycloak)
            expertToken = TestUtils.testKeycloakGetExpertToken(keycloak)
            vendorToken = TestUtils.testKeycloakGetVendorToken(keycloak)
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
    lateinit var brandRepository:BrandRepository

    @Test
    fun getExistingBrandUnauthorized() {
        val uri = URI("http://localhost:$port/API/public/brands/")

        val brand = Brand()

        brand.name = "Apple"

        brandRepository.save(brand)

        val entity = HttpEntity(null, null)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        brandRepository.delete(brand)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(body.size, 1)


    }

    @Test
    fun getExistingBrandManager() {
        val uri = URI("http://localhost:$port/API/public/brands/")

        val brand = Brand()

        brand.name = "Apple"

        brandRepository.save(brand)

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        brandRepository.delete(brand)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(body.size, 1)


    }

    @Test
    fun getExistingBrandClient() {
        val uri = URI("http://localhost:$port/API/public/brands/")

        val brand = Brand()

        brand.name = "Apple"

        brandRepository.save(brand)

        val entity = TestUtils.testEntityHeader(null, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        brandRepository.delete(brand)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(body.size, 1)


    }

    @Test
    fun getExistingBrandExpert() {
        val uri = URI("http://localhost:$port/API/public/brands/")

        val brand = Brand()

        brand.name = "Apple"

        brandRepository.save(brand)

        val entity = TestUtils.testEntityHeader(null, expertToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        brandRepository.delete(brand)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(body.size, 1)


    }


    @Test
    fun getExistingBrandVendor() {
        val uri = URI("http://localhost:$port/API/public/brands/")

        val brand = Brand()

        brand.name = "Apple"

        brandRepository.save(brand)

        val entity = TestUtils.testEntityHeader(null, vendorToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        brandRepository.delete(brand)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(body.size, 1)


    }

    @Test
    fun addBrandUnauthorizedClient() {
        val uri = URI("http://localhost:$port/API/manager/brand/")

        val brandEntity = BrandDTO(
            "Apple"
        )


        val entity = TestUtils.testEntityHeader(brandEntity, clientToken)


        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
            String::class.java
        )



        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)
    }

    @Test
    fun addBrandUnauthorizedExpert() {
        val uri = URI("http://localhost:$port/API/manager/brand/")

        val brandEntity = BrandDTO(
            "Apple"
        )


        val entity = TestUtils.testEntityHeader(brandEntity, expertToken)


        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
            String::class.java
        )



        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)
    }

    @Test
    fun addBrandUnauthorizedVendor() {
        val uri = URI("http://localhost:$port/API/manager/brand/")

        val brandEntity = BrandDTO(
            "Apple"
        )


        val entity = TestUtils.testEntityHeader(brandEntity, vendorToken)


        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
            String::class.java
        )



        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)
    }

    @Test
    fun addBrandAuthorizedManager() {
        val uri = URI("http://localhost:$port/API/manager/brand/")


        val brandEntity = BrandDTO(
            "Apple"
        )


        val entity = TestUtils.testEntityHeader(brandEntity, managerToken)


        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
            String::class.java
        )



        Assertions.assertEquals(HttpStatus.CREATED, result.statusCode)

        val createdBrand = brandRepository.findByName("Apple")

        Assertions.assertNotNull(createdBrand)
        brandRepository.delete(createdBrand!!)
    }

    @Test
    fun addBrandDuplicate() {
        val uri = URI("http://localhost:$port/API/manager/brand/")

        val brand = Brand()
        brand.name = "Apple"
        brandRepository.save(brand)

        val brandEntity = BrandDTO(
            "Apple"
        )


        val entity = TestUtils.testEntityHeader(brandEntity, managerToken)


        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
            String::class.java
        )


        brandRepository.delete(brand)
        Assertions.assertEquals(HttpStatus.CONFLICT, result.statusCode)

    }

    @Test
    fun addBrandWrongFormat() {
        val uri = URI("http://localhost:$port/API/manager/brand/")

        val brand = Brand()
        brand.name = "Apple"
        brandRepository.save(brand)

        val brandEntity = object {
            val value = "Apple"
        }


        val entity = TestUtils.testEntityHeader(brandEntity, managerToken)


        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
            String::class.java
        )

        brandRepository.delete(brand)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)

    }

}

