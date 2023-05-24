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

            val realmName = "SpringBootKeycloak"
            val clientId = "springboot-keycloak-client"

            val manager = UserRepresentation()
            manager.email = "manager@polito.it"
            manager.username = "manager_01"
            manager.isEnabled = true

            val client = UserRepresentation()
            client.email = "client@polito.it"
            client.username = "client_01"
            client.isEnabled = true

            val expert = UserRepresentation()
            expert.email = "expert@polito.it"
            expert.username = "expert_01"
            expert.isEnabled = true

            val credential = CredentialRepresentation()
            credential.isTemporary = false
            credential.type = CredentialRepresentation.PASSWORD
            credential.value = "password"

            keycloak.keycloakAdminClient.realm(realmName).users().create(manager)
            keycloak.keycloakAdminClient.realm(realmName).users().create(client)
            keycloak.keycloakAdminClient.realm(realmName).users().create(expert)


            val createdManager =
                keycloak.keycloakAdminClient.realm(realmName).users().search(manager.email)[0]
            val createdClient =
                keycloak.keycloakAdminClient.realm(realmName).users().search(client.email)[0]
            val createdExpert =
                keycloak.keycloakAdminClient.realm(realmName).users().search(expert.email)[0]

            val roleManager = keycloak.keycloakAdminClient.realm(realmName).roles().get("app_manager")
            val roleClient = keycloak.keycloakAdminClient.realm(realmName).roles().get("app_client")
            val roleExpert = keycloak.keycloakAdminClient.realm(realmName).roles().get("app_expert")

            keycloak.keycloakAdminClient.realm(realmName).users().get(createdManager.id).resetPassword(credential)
            keycloak.keycloakAdminClient.realm(realmName).users().get(createdManager.id).roles().realmLevel().add(listOf(roleManager.toRepresentation()))

            keycloak.keycloakAdminClient.realm(realmName).users().get(createdClient.id).resetPassword(credential)
            keycloak.keycloakAdminClient.realm(realmName).users().get(createdClient.id).roles().realmLevel().add(listOf(roleClient.toRepresentation()))

            keycloak.keycloakAdminClient.realm(realmName).users().get(createdExpert.id).resetPassword(credential)
            keycloak.keycloakAdminClient.realm(realmName).users().get(createdExpert.id).roles().realmLevel().add(listOf(roleExpert.toRepresentation()))


            val kcManager = KeycloakBuilder
                .builder()
                .serverUrl(keycloak.authServerUrl)
                .realm(realmName)
                .clientId(clientId)
                .username(manager.email)
                .password("password")
                .build()

            val kcClient = KeycloakBuilder
                .builder()
                .serverUrl(keycloak.authServerUrl)
                .realm(realmName)
                .clientId(clientId)
                .username(client.email)
                .password("password")
                .build()

            val kcExpert = KeycloakBuilder
                .builder()
                .serverUrl(keycloak.authServerUrl)
                .realm(realmName)
                .clientId(clientId)
                .username(expert.email)
                .password("password")
                .build()

            kcManager.tokenManager().grantToken().expiresIn = 3600
            kcClient.tokenManager().grantToken().expiresIn = 3600
            kcExpert.tokenManager().grantToken().expiresIn = 3600


            managerToken = kcManager.tokenManager().accessToken.token
            clientToken = kcClient.tokenManager().accessToken.token
            expertToken = kcExpert.tokenManager().accessToken.token
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
        val url = "http://localhost:$port/API/products/"
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

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)

    }
    @Test
    @DirtiesContext
    fun getExistingProductsManager() {
        val url = "http://localhost:$port/API/products/"
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
        val url = "http://localhost:$port/API/products/"
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
        val url = "http://localhost:$port/API/products/"
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
        val url = "http://localhost:$port/API/products/0000000000000"
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
        val url = "http://localhost:$port/API/products/0000000000000"
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
        val url = "http://localhost:$port/API/products/0000000000000"
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
        val url = "http://localhost:$port/API/products/0000000000003"
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

        val entity = HttpEntity(null, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)

        productRepository.delete(product1)
        productRepository.delete(product2)
        productRepository.delete(product3)
    }

    @Test
    @DirtiesContext
    fun getNonExistingProductManager() {
        val url = "http://localhost:$port/API/products/0000000000003"
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
        val url = "http://localhost:$port/API/products/0000000000003"
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
        val url = "http://localhost:$port/API/products/0000000000003"
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
        val url = "http://localhost:$port/API/products/000000000000"
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
        val url = "http://localhost:$port/API/products/000000000000"
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
        val url = "http://localhost:$port/API/products/000000000000"
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

