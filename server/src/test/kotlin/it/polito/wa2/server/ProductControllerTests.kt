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
        val url = "http://localhost:$port/API/public/products/"
        val uri = URI(url)
        val json = BasicJsonParser()

        val product1 = Product()
        product1.productId = "0000000000000"
        product1.name = "PC Omen Intel i7"
        product1.brand = "HP"

        val product2 = Product()
        product2.productId = "0000000000001"
        product2.name = "iPad 7th Generation"
        product2.brand = "Apple"

        val product3 = Product()
        product3.productId = "0000000000002"
        product3.name = "Surface Pro 10 inches"
        product3.brand = "Microsoft"

        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(null, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

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
        val url = "http://localhost:$port/API/public/products/"
        val uri = URI(url)
        val json = BasicJsonParser()

        val product1 = Product()
        product1.productId = "0000000000000"
        product1.name = "PC Omen Intel i7"
        product1.brand = "HP"

        val product2 = Product()
        product2.productId = "0000000000001"
        product2.name = "iPad 7th Generation"
        product2.brand = "Apple"

        val product3 = Product()
        product3.productId = "0000000000002"
        product3.name = "Surface Pro 10 inches"
        product3.brand = "Microsoft"

        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

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
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

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
        val url = "http://localhost:$port/API/public/products/"
        val uri = URI(url)
        val json = BasicJsonParser()

        val product1 = Product()
        product1.productId = "0000000000000"
        product1.name = "PC Omen Intel i7"
        product1.brand = "HP"

        val product2 = Product()
        product2.productId = "0000000000001"
        product2.name = "iPad 7th Generation"
        product2.brand = "Apple"

        val product3 = Product()
        product3.productId = "0000000000002"
        product3.name = "Surface Pro 10 inches"
        product3.brand = "Microsoft"

        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

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
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

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
        val url = "http://localhost:$port/API/public/products/"
        val uri = URI(url)
        val json = BasicJsonParser()

        val product1 = Product()
        product1.productId = "0000000000000"
        product1.name = "PC Omen Intel i7"
        product1.brand = "HP"

        val product2 = Product()
        product2.productId = "0000000000001"
        product2.name = "iPad 7th Generation"
        product2.brand = "Apple"

        val product3 = Product()
        product3.productId = "0000000000002"
        product3.name = "Surface Pro 10 inches"
        product3.brand = "Microsoft"

        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

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
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

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
        val url = "http://localhost:$port/API/public/products/0000000000000"
        val uri = URI(url)
        val json = BasicJsonParser()

        val product1 = Product()
        product1.productId = "0000000000000"
        product1.name = "PC Omen Intel i7"
        product1.brand = "HP"

        val product2 = Product()
        product2.productId = "0000000000001"
        product2.name = "iPad 7th Generation"
        product2.brand = "Apple"

        val product3 = Product()
        product3.productId = "0000000000002"
        product3.name = "Surface Pro 10 inches"
        product3.brand = "Microsoft"

        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

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
        val url = "http://localhost:$port/API/public/products/0000000000000"
        val uri = URI(url)
        val json = BasicJsonParser()

        val product1 = Product()
        product1.productId = "0000000000000"
        product1.name = "PC Omen Intel i7"
        product1.brand = "HP"

        val product2 = Product()
        product2.productId = "0000000000001"
        product2.name = "iPad 7th Generation"
        product2.brand = "Apple"

        val product3 = Product()
        product3.productId = "0000000000002"
        product3.name = "Surface Pro 10 inches"
        product3.brand = "Microsoft"

        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

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
        val body = json.parseMap(result.body)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

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
        val url = "http://localhost:$port/API/public/products/0000000000000"
        val uri = URI(url)
        val json = BasicJsonParser()

        val product1 = Product()
        product1.productId = "0000000000000"
        product1.name = "PC Omen Intel i7"
        product1.brand = "HP"

        val product2 = Product()
        product2.productId = "0000000000001"
        product2.name = "iPad 7th Generation"
        product2.brand = "Apple"

        val product3 = Product()
        product3.productId = "0000000000002"
        product3.name = "Surface Pro 10 inches"
        product3.brand = "Microsoft"

        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

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
        val body = json.parseMap(result.body)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

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
        val url = "http://localhost:$port/API/public/products/0000000000000"
        val uri = URI(url)
        val json = BasicJsonParser()
        val product1 = Product()
        product1.productId = "0000000000000"
        product1.name = "PC Omen Intel i7"
        product1.brand = "HP"

        val product2 = Product()
        product2.productId = "0000000000001"
        product2.name = "iPad 7th Generation"
        product2.brand = "Apple"

        val product3 = Product()
        product3.productId = "0000000000002"
        product3.name = "Surface Pro 10 inches"
        product3.brand = "Microsoft"

        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity(null, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        val body = json.parseMap(result.body)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

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
        val url = "http://localhost:$port/API/public/products/0000000000003"
        val uri = URI(url)

        val product1 = Product()
        product1.productId = "0000000000000"
        product1.name = "PC Omen Intel i7"
        product1.brand = "HP"

        val product2 = Product()
        product2.productId = "0000000000001"
        product2.name = "iPad 7th Generation"
        product2.brand = "Apple"

        val product3 = Product()
        product3.productId = "0000000000002"
        product3.name = "Surface Pro 10 inches"
        product3.brand = "Microsoft"

        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

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

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

    }

    @Test
    @DirtiesContext
    fun getNonExistingProductClient() {
        val url = "http://localhost:$port/API/public/products/0000000000003"
        val uri = URI(url)

        val product1 = Product()
        product1.productId = "0000000000000"
        product1.name = "PC Omen Intel i7"
        product1.brand = "HP"

        val product2 = Product()
        product2.productId = "0000000000001"
        product2.name = "iPad 7th Generation"
        product2.brand = "Apple"

        val product3 = Product()
        product3.productId = "0000000000002"
        product3.name = "Surface Pro 10 inches"
        product3.brand = "Microsoft"

        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

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
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

    }

    @Test
    @DirtiesContext
    fun getNonExistingProductExpert() {
        val url = "http://localhost:$port/API/public/products/0000000000003"
        val uri = URI(url)

        val product1 = Product()
        product1.productId = "0000000000000"
        product1.name = "PC Omen Intel i7"
        product1.brand = "HP"

        val product2 = Product()
        product2.productId = "0000000000001"
        product2.name = "iPad 7th Generation"
        product2.brand = "Apple"

        val product3 = Product()
        product3.productId = "0000000000002"
        product3.name = "Surface Pro 10 inches"
        product3.brand = "Microsoft"

        productRepository.save(product1)
        productRepository.save(product2)
        productRepository.save(product3)

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
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

    }

    @Test
    @DirtiesContext
    fun getProductWrongIdManager() {
        val url = "http://localhost:$port/API/public/products/000000000000"
        val uri = URI(url)
        val json = BasicJsonParser()

        val product1 = Product()
        product1.productId = "0000000000000"
        product1.name = "PC Omen Intel i7"
        product1.brand = "HP"

        productRepository.save(product1)

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
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        productRepository.delete(product1)

    }

    @Test
    @DirtiesContext
    fun getProductWrongIdClient() {
        val url = "http://localhost:$port/API/public/products/000000000000"
        val uri = URI(url)
        val json = BasicJsonParser()

        val product1 = Product()
        product1.productId = "0000000000000"
        product1.name = "PC Omen Intel i7"
        product1.brand = "HP"

        productRepository.save(product1)

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
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        productRepository.delete(product1)

    }

    @Test
    @DirtiesContext
    fun getProductWrongIdExpert() {
        val url = "http://localhost:$port/API/public/products/000000000000"
        val uri = URI(url)
        val json = BasicJsonParser()

        val product1 = Product()
        product1.productId = "0000000000000"
        product1.name = "PC Omen Intel i7"
        product1.brand = "HP"

        productRepository.save(product1)

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
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        productRepository.delete(product1)

    }
}

