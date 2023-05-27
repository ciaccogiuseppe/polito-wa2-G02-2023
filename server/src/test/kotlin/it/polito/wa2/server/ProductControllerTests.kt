package it.polito.wa2.server

import dasniko.testcontainers.keycloak.KeycloakContainer
import it.polito.wa2.server.products.Product
import it.polito.wa2.server.products.ProductRepository
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
class ProductControllerTests {
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
    lateinit var productRepository: ProductRepository

    @Test
    @DirtiesContext
    fun getExistingProductsUnauthorized() {
        val uri = URI("http://localhost:$port/API/public/products/")

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", "Apple")
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", "Microsoft")
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)


        val entity = HttpEntity(null, null)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )


        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(body.size, 3)

        Assertions.assertEquals(true, body.any{a -> a["productId"] == product1.productId})
        Assertions.assertEquals(true, body.any{a -> a["productId"] == product2.productId})
        Assertions.assertEquals(true, body.any{a -> a["productId"] == product3.productId})

        Assertions.assertEquals(true, body.any{a -> a["name"] == product1.name})
        Assertions.assertEquals(true, body.any{a -> a["name"] == product2.name})
        Assertions.assertEquals(true, body.any{a -> a["name"] == product3.name})

        Assertions.assertEquals(true, body.any{a -> a["brand"] == product1.brand})
        Assertions.assertEquals(true, body.any{a -> a["brand"] == product2.brand})
        Assertions.assertEquals(true, body.any{a -> a["brand"] == product3.brand})

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

    }
    @Test
    @DirtiesContext
    fun getExistingProductsManager() {
        val uri = URI("http://localhost:$port/API/public/products/")

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", "Apple")
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", "Microsoft")
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(body.size, 3)

        Assertions.assertEquals(true, body.any{a -> a["productId"] == product1.productId})
        Assertions.assertEquals(true, body.any{a -> a["productId"] == product2.productId})
        Assertions.assertEquals(true, body.any{a -> a["productId"] == product3.productId})

        Assertions.assertEquals(true, body.any{a -> a["name"] == product1.name})
        Assertions.assertEquals(true, body.any{a -> a["name"] == product2.name})
        Assertions.assertEquals(true, body.any{a -> a["name"] == product3.name})

        Assertions.assertEquals(true, body.any{a -> a["brand"] == product1.brand})
        Assertions.assertEquals(true, body.any{a -> a["brand"] == product2.brand})
        Assertions.assertEquals(true, body.any{a -> a["brand"] == product3.brand})

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

    }

    @Test
    @DirtiesContext
    fun getExistingProductsClient() {
        val uri = URI("http://localhost:$port/API/public/products/")

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", "Apple")
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", "Microsoft")
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val entity = TestUtils.testEntityHeader(null, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(body.size, 3)

        Assertions.assertEquals(true, body.any{a -> a["productId"] == product1.productId})
        Assertions.assertEquals(true, body.any{a -> a["productId"] == product2.productId})
        Assertions.assertEquals(true, body.any{a -> a["productId"] == product3.productId})

        Assertions.assertEquals(true, body.any{a -> a["name"] == product1.name})
        Assertions.assertEquals(true, body.any{a -> a["name"] == product2.name})
        Assertions.assertEquals(true, body.any{a -> a["name"] == product3.name})

        Assertions.assertEquals(true, body.any{a -> a["brand"] == product1.brand})
        Assertions.assertEquals(true, body.any{a -> a["brand"] == product2.brand})
        Assertions.assertEquals(true, body.any{a -> a["brand"] == product3.brand})

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

    }

    @Test
    @DirtiesContext
    fun getExistingProductsExpert() {
        val uri = URI("http://localhost:$port/API/public/products/")

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", "Apple")
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", "Microsoft")
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val entity = TestUtils.testEntityHeader(null, expertToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(body.size, 3)

        Assertions.assertEquals(true, body.any{a -> a["productId"] == product1.productId})
        Assertions.assertEquals(true, body.any{a -> a["productId"] == product2.productId})
        Assertions.assertEquals(true, body.any{a -> a["productId"] == product3.productId})

        Assertions.assertEquals(true, body.any{a -> a["name"] == product1.name})
        Assertions.assertEquals(true, body.any{a -> a["name"] == product2.name})
        Assertions.assertEquals(true, body.any{a -> a["name"] == product3.name})

        Assertions.assertEquals(true, body.any{a -> a["brand"] == product1.brand})
        Assertions.assertEquals(true, body.any{a -> a["brand"] == product2.brand})
        Assertions.assertEquals(true, body.any{a -> a["brand"] == product3.brand})

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

    }


    @Test
    @DirtiesContext
    fun getExistingProductManager() {
        val uri = URI("http://localhost:$port/API/public/products/0000000000000")

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", "Apple")
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", "Microsoft")
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseMap(result.body)
        Assertions.assertEquals(product1.productId, body["productId"])
        Assertions.assertEquals(product1.name, body["name"])
        Assertions.assertEquals(product1.brand, body["brand"])

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

    }

    @Test
    @DirtiesContext
    fun getExistingProductClient() {
        val uri = URI("http://localhost:$port/API/public/products/0000000000000")

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", "Apple")
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", "Microsoft")
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val entity = TestUtils.testEntityHeader(null, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseMap(result.body)
        Assertions.assertEquals(product1.productId, body["productId"])
        Assertions.assertEquals(product1.name, body["name"])
        Assertions.assertEquals(product1.brand, body["brand"])

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

    }

    @Test
    @DirtiesContext
    fun getExistingProductExpert() {
        val uri = URI("http://localhost:$port/API/public/products/0000000000000")

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", "Apple")
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", "Microsoft")
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val entity = TestUtils.testEntityHeader(null, expertToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseMap(result.body)
        Assertions.assertEquals(product1.productId, body["productId"])
        Assertions.assertEquals(product1.name, body["name"])
        Assertions.assertEquals(product1.brand, body["brand"])

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

    }

    @Test
    @DirtiesContext
    fun getProductUnauthorized() {
        val uri = URI("http://localhost:$port/API/public/products/0000000000000")

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", "Apple")
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", "Microsoft")
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val entity = HttpEntity(null, null)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseMap(result.body)
        Assertions.assertEquals(product1.productId, body["productId"])
        Assertions.assertEquals(product1.name, body["name"])
        Assertions.assertEquals(product1.brand, body["brand"])

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)
    }

    @Test
    @DirtiesContext
    fun getNonExistingProductManager() {
        val uri = URI("http://localhost:$port/API/public/products/0000000000003")

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", "Apple")
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", "Microsoft")
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

    }

    @Test
    @DirtiesContext
    fun getNonExistingProductClient() {
        val uri = URI("http://localhost:$port/API/public/products/0000000000003")

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", "Apple")
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", "Microsoft")
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val entity = TestUtils.testEntityHeader(null, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

    }

    @Test
    @DirtiesContext
    fun getNonExistingProductExpert() {
        val uri = URI("http://localhost:$port/API/public/products/0000000000003")

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        val product2 = TestUtils.testProduct("0000000000001", "iPad 7th Generation", "Apple")
        val product3 = TestUtils.testProduct("0000000000002", "Surface Pro 10 inches", "Microsoft")
        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val entity = TestUtils.testEntityHeader(null, expertToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

    }

    @Test
    @DirtiesContext
    fun getProductWrongIdManager() {
        val uri = URI("http://localhost:$port/API/public/products/000000000000")

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product1)

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        productRepository.delete(product1)

    }

    @Test
    @DirtiesContext
    fun getProductWrongIdClient() {
        val uri = URI("http://localhost:$port/API/public/products/000000000000")

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product1)

        val entity = TestUtils.testEntityHeader(null, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        productRepository.delete(product1)

    }

    @Test
    @DirtiesContext
    fun getProductWrongIdExpert() {
        val uri = URI("http://localhost:$port/API/public/products/000000000000")

        val product1 = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product1)

        val entity = TestUtils.testEntityHeader(null, expertToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        productRepository.delete(product1)

    }
}

