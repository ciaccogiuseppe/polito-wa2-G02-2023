package it.polito.wa2.server


import dasniko.testcontainers.keycloak.KeycloakContainer
import it.polito.wa2.server.products.Product
import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileRole
import it.polito.wa2.server.ticketing.ticket.*
import it.polito.wa2.server.ticketing.tickethistory.TicketHistoryRepository
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
import java.sql.Timestamp
import java.util.*


@Testcontainers
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
class TicketControllerTests {
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
    lateinit var profileRepository: ProfileRepository
    @Autowired
    lateinit var productRepository: ProductRepository
    @Autowired
    lateinit var ticketRepository: TicketRepository
    @Autowired
    lateinit var ticketHistoryRepository: TicketHistoryRepository
    @Test
    @DirtiesContext
    fun getExistingTicketManager() {
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket = Ticket()
        ticket.createdTimestamp = Timestamp(0)
        ticket.product = product
        ticket.customer = customer
        ticket.status = TicketStatus.IN_PROGRESS
        ticket.expert = expert
        ticket.priority = 2
        ticket.title = "Ticket sample"
        ticket.description = "Ticket description sample"

        ticketRepository.save(ticket)

        val url = "http://localhost:$port/API/manager/ticketing/${ticket.ticketId}"
        val uri = URI(url)
        val json = BasicJsonParser()

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
        Assertions.assertEquals(product.productId, body["productId"])
        Assertions.assertEquals(customer.email, body["customerEmail"])
        Assertions.assertEquals(expert.email, body["expertEmail"])
        Assertions.assertNotNull(body["status"])
        Assertions.assertEquals(ticket.status, TicketStatus.valueOf(body["status"]!!.toString()))
        Assertions.assertEquals(ticket.priority.toLong(), body["priority"])
        Assertions.assertEquals(ticket.description, body["description"])
        Assertions.assertEquals(ticket.title, body["title"])

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getExistingTicketAuthorizedClient() {
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket = Ticket()
        ticket.createdTimestamp = Timestamp(0)
        ticket.product = product
        ticket.customer = customer
        ticket.status = TicketStatus.IN_PROGRESS
        ticket.expert = expert
        ticket.priority = 2
        ticket.title = "Ticket sample"
        ticket.description = "Ticket description sample"

        ticketRepository.save(ticket)

        val url = "http://localhost:$port/API/client/ticketing/${ticket.ticketId}"
        val uri = URI(url)
        val json = BasicJsonParser()

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
        Assertions.assertEquals(product.productId, body["productId"])
        Assertions.assertEquals(customer.email, body["customerEmail"])
        Assertions.assertEquals(expert.email, body["expertEmail"])
        Assertions.assertNotNull(body["status"])
        Assertions.assertEquals(ticket.status, TicketStatus.valueOf(body["status"]!!.toString()))
        Assertions.assertEquals(ticket.priority.toLong(), body["priority"])
        Assertions.assertEquals(ticket.description, body["description"])
        Assertions.assertEquals(ticket.title, body["title"])

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getExistingTicketAuthorizedExpert() {
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket = Ticket()
        ticket.createdTimestamp = Timestamp(0)
        ticket.product = product
        ticket.customer = customer
        ticket.status = TicketStatus.IN_PROGRESS
        ticket.expert = expert
        ticket.priority = 2
        ticket.title = "Ticket sample"
        ticket.description = "Ticket description sample"

        ticketRepository.save(ticket)

        val url = "http://localhost:$port/API/expert/ticketing/${ticket.ticketId}"
        val uri = URI(url)
        val json = BasicJsonParser()

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
        Assertions.assertEquals(product.productId, body["productId"])
        Assertions.assertEquals(customer.email, body["customerEmail"])
        Assertions.assertEquals(expert.email, body["expertEmail"])
        Assertions.assertNotNull(body["status"])
        Assertions.assertEquals(ticket.status, TicketStatus.valueOf(body["status"]!!.toString()))
        Assertions.assertEquals(ticket.priority.toLong(), body["priority"])
        Assertions.assertEquals(ticket.description, body["description"])
        Assertions.assertEquals(ticket.title, body["title"])

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getExistingTicketForbiddenClient() {
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket = Ticket()
        ticket.createdTimestamp = Timestamp(0)
        ticket.product = product
        ticket.customer = customer
        ticket.status = TicketStatus.IN_PROGRESS
        ticket.expert = expert
        ticket.priority = 2
        ticket.title = "Ticket sample"
        ticket.description = "Ticket description sample"

        ticketRepository.save(ticket)

        val url = "http://localhost:$port/API/client/ticketing/${ticket.ticketId}"
        val uri = URI(url)
        val json = BasicJsonParser()

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

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getExistingTicketForbiddenExpert() {
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket = Ticket()
        ticket.createdTimestamp = Timestamp(0)
        ticket.product = product
        ticket.customer = customer
        ticket.status = TicketStatus.IN_PROGRESS
        ticket.expert = expert
        ticket.priority = 2
        ticket.title = "Ticket sample"
        ticket.description = "Ticket description sample"

        ticketRepository.save(ticket)

        val url = "http://localhost:$port/API/expert/ticketing/${ticket.ticketId}"
        val uri = URI(url)
        val json = BasicJsonParser()

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
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }


    @Test
    @DirtiesContext
    fun getNonExistingTicket() {
        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        val url = "http://localhost:$port/API/manager/ticketing/1"
        val uri = URI(url)
        val json = BasicJsonParser()


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
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getWrongIdTicket() {
        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        val url = "http://localhost:$port/API/manager/ticketing/abc"
        val uri = URI(url)
        val json = BasicJsonParser()

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
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getFilteredTicketsSingleCustomerFilter(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val customer2 = Profile()
        customer2.email = "client2@polito.it"
        customer2.name = "Mario"
        customer2.surname = "Blu"
        customer2.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "client3@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val expert2 = Profile()
        expert2.email = "luigi.bianchi@polito.it"
        expert2.name = "Luigi"
        expert2.surname = "Bianchi"
        expert2.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket1 = Ticket()
        ticket1.createdTimestamp = Timestamp(0)
        ticket1.product = product
        ticket1.customer = customer
        ticket1.status = TicketStatus.IN_PROGRESS
        ticket1.expert = expert
        ticket1.priority = 2
        ticket1.title = "Ticket sample"
        ticket1.description = "Ticket description sample"

        val ticket2 = Ticket()
        ticket2.createdTimestamp = Timestamp(0)
        ticket2.product = product
        ticket2.customer = customer
        ticket2.status = TicketStatus.IN_PROGRESS
        ticket2.expert = expert2
        ticket2.priority = 2
        ticket2.title = "Ticket sample"
        ticket2.description = "Ticket description sample"

        val ticket3 = Ticket()
        ticket3.createdTimestamp = Timestamp(0)
        ticket3.product = product
        ticket3.customer = customer2
        ticket3.status = TicketStatus.IN_PROGRESS
        ticket3.expert = expert
        ticket3.priority = 2
        ticket3.title = "Ticket sample"
        ticket3.description = "Ticket description sample"

        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val url = "http://localhost:$port/API/manager/ticketing/filter?customerEmail=${customer.email}"
        val uri = URI(url)
        val json = BasicJsonParser()

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
        println(result.body)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(2, body.size)

        ticketRepository.delete(ticket3)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket1)
        profileRepository.delete(customer)
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        profileRepository.delete(expert2)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getFilteredTicketsSingleCustomerFilterAuthorizedClient(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val customer2 = Profile()
        customer2.email = "mario.blu@polito.it"
        customer2.name = "Mario"
        customer2.surname = "Blu"
        customer2.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val expert2 = Profile()
        expert2.email = "luigi.bianchi@polito.it"
        expert2.name = "Luigi"
        expert2.surname = "Bianchi"
        expert2.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket1 = Ticket()
        ticket1.createdTimestamp = Timestamp(0)
        ticket1.product = product
        ticket1.customer = customer
        ticket1.status = TicketStatus.IN_PROGRESS
        ticket1.expert = expert
        ticket1.priority = 2
        ticket1.title = "Ticket sample"
        ticket1.description = "Ticket description sample"

        val ticket2 = Ticket()
        ticket2.createdTimestamp = Timestamp(0)
        ticket2.product = product
        ticket2.customer = customer
        ticket2.status = TicketStatus.IN_PROGRESS
        ticket2.expert = expert2
        ticket2.priority = 2
        ticket2.title = "Ticket sample"
        ticket2.description = "Ticket description sample"

        val ticket3 = Ticket()
        ticket3.createdTimestamp = Timestamp(0)
        ticket3.product = product
        ticket3.customer = customer2
        ticket3.status = TicketStatus.IN_PROGRESS
        ticket3.expert = expert
        ticket3.priority = 2
        ticket3.title = "Ticket sample"
        ticket3.description = "Ticket description sample"

        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val url = "http://localhost:$port/API/client/ticketing/filter?customerEmail=${customer.email}"
        val uri = URI(url)
        val json = BasicJsonParser()

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

        Assertions.assertEquals(2, body.size)

        ticketRepository.delete(ticket3)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket1)
        profileRepository.delete(customer)
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        profileRepository.delete(expert2)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getFilteredTicketsSingleCustomerFilterOtherClient(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val customer2 = Profile()
        customer2.email = "mario.blu@polito.it"
        customer2.name = "Mario"
        customer2.surname = "Blu"
        customer2.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val expert2 = Profile()
        expert2.email = "luigi.bianchi@polito.it"
        expert2.name = "Luigi"
        expert2.surname = "Bianchi"
        expert2.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket1 = Ticket()
        ticket1.createdTimestamp = Timestamp(0)
        ticket1.product = product
        ticket1.customer = customer
        ticket1.status = TicketStatus.IN_PROGRESS
        ticket1.expert = expert
        ticket1.priority = 2
        ticket1.title = "Ticket sample"
        ticket1.description = "Ticket description sample"

        val ticket2 = Ticket()
        ticket2.createdTimestamp = Timestamp(0)
        ticket2.product = product
        ticket2.customer = customer
        ticket2.status = TicketStatus.IN_PROGRESS
        ticket2.expert = expert2
        ticket2.priority = 2
        ticket2.title = "Ticket sample"
        ticket2.description = "Ticket description sample"

        val ticket3 = Ticket()
        ticket3.createdTimestamp = Timestamp(0)
        ticket3.product = product
        ticket3.customer = customer2
        ticket3.status = TicketStatus.IN_PROGRESS
        ticket3.expert = expert
        ticket3.priority = 2
        ticket3.title = "Ticket sample"
        ticket3.description = "Ticket description sample"

        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val url = "http://localhost:$port/API/client/ticketing/filter?customerEmail=${customer2.email}"
        val uri = URI(url)
        val json = BasicJsonParser()

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
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(0, body.size)

        ticketRepository.delete(ticket3)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket1)
        profileRepository.delete(customer)
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        profileRepository.delete(expert2)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getFilteredTicketsMultipleUserFilter(){
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val customer2 = Profile()
        customer2.email = "mario.blu@polito.it"
        customer2.name = "Mario"
        customer2.surname = "Blu"
        customer2.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val expert2 = Profile()
        expert2.email = "luigi.bianchi@polito.it"
        expert2.name = "Luigi"
        expert2.surname = "Bianchi"
        expert2.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket1 = Ticket()
        ticket1.createdTimestamp = Timestamp(0)
        ticket1.product = product
        ticket1.customer = customer
        ticket1.status = TicketStatus.IN_PROGRESS
        ticket1.expert = expert
        ticket1.priority = 2
        ticket1.title = "Ticket sample"
        ticket1.description = "Ticket description sample"

        val ticket2 = Ticket()
        ticket2.createdTimestamp = Timestamp(0)
        ticket2.product = product
        ticket2.customer = customer
        ticket2.status = TicketStatus.IN_PROGRESS
        ticket2.expert = expert2
        ticket2.priority = 2
        ticket2.title = "Ticket sample"
        ticket2.description = "Ticket description sample"

        val ticket3 = Ticket()
        ticket3.createdTimestamp = Timestamp(0)
        ticket3.product = product
        ticket3.customer = customer2
        ticket3.status = TicketStatus.IN_PROGRESS
        ticket3.expert = expert
        ticket3.priority = 2
        ticket3.title = "Ticket sample"
        ticket3.description = "Ticket description sample"

        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val url = "http://localhost:$port/API/manager/ticketing/filter?customerEmail=${customer.email}&expertEmail=${expert2.email}"
        val uri = URI(url)
        val json = BasicJsonParser()

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

        Assertions.assertEquals(1, body.size)

        ticketRepository.delete(ticket3)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket1)
        profileRepository.delete(customer)
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        profileRepository.delete(expert2)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getFilteredTicketsMultipleUserFilterOneClientOnly(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val customer2 = Profile()
        customer2.email = "mario.blu@polito.it"
        customer2.name = "Mario"
        customer2.surname = "Blu"
        customer2.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val expert2 = Profile()
        expert2.email = "luigi.bianchi@polito.it"
        expert2.name = "Luigi"
        expert2.surname = "Bianchi"
        expert2.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket1 = Ticket()
        ticket1.createdTimestamp = Timestamp(0)
        ticket1.product = product
        ticket1.customer = customer
        ticket1.status = TicketStatus.IN_PROGRESS
        ticket1.expert = expert
        ticket1.priority = 2
        ticket1.title = "Ticket sample"
        ticket1.description = "Ticket description sample"

        val ticket2 = Ticket()
        ticket2.createdTimestamp = Timestamp(0)
        ticket2.product = product
        ticket2.customer = customer
        ticket2.status = TicketStatus.IN_PROGRESS
        ticket2.expert = expert2
        ticket2.priority = 2
        ticket2.title = "Ticket sample"
        ticket2.description = "Ticket description sample"

        val ticket3 = Ticket()
        ticket3.createdTimestamp = Timestamp(0)
        ticket3.product = product
        ticket3.customer = customer2
        ticket3.status = TicketStatus.IN_PROGRESS
        ticket3.expert = expert
        ticket3.priority = 2
        ticket3.title = "Ticket sample"
        ticket3.description = "Ticket description sample"

        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val url = "http://localhost:$port/API/client/ticketing/filter?customerEmail=${customer.email}&expertEmail=${expert2.email}"
        val uri = URI(url)
        val json = BasicJsonParser()

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

        Assertions.assertEquals(1, body.size)

        ticketRepository.delete(ticket3)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket1)
        profileRepository.delete(customer)
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        profileRepository.delete(expert2)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getFilteredTicketsTimeRange(){
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val customer2 = Profile()
        customer2.email = "mario.blu@polito.it"
        customer2.name = "Mario"
        customer2.surname = "Blu"
        customer2.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val expert2 = Profile()
        expert2.email = "luigi.bianchi@polito.it"
        expert2.name = "Luigi"
        expert2.surname = "Bianchi"
        expert2.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket1 = Ticket()
        ticket1.createdTimestamp = Timestamp(0)
        ticket1.product = product
        ticket1.customer = customer
        ticket1.status = TicketStatus.IN_PROGRESS
        ticket1.expert = expert
        ticket1.priority = 2
        ticket1.title = "Ticket sample"
        ticket1.description = "Ticket description sample"

        val ticket2 = Ticket()
        ticket2.createdTimestamp = Timestamp(1)
        ticket2.product = product
        ticket2.customer = customer
        ticket2.status = TicketStatus.IN_PROGRESS
        ticket2.expert = expert2
        ticket2.priority = 2
        ticket2.title = "Ticket sample"
        ticket2.description = "Ticket description sample"

        val ticket3 = Ticket()
        ticket3.createdTimestamp = Timestamp(2)
        ticket3.product = product
        ticket3.customer = customer2
        ticket3.status = TicketStatus.IN_PROGRESS
        ticket3.expert = expert
        ticket3.priority = 2
        ticket3.title = "Ticket sample"
        ticket3.description = "Ticket description sample"

        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val url = "http://localhost:$port/API/manager/ticketing/filter?createdAfter=${Timestamp(0).toLocalDateTime()}&createdBefore=${Timestamp(1).toLocalDateTime()}"
        val uri = URI(url)
        val json = BasicJsonParser()

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

        Assertions.assertEquals(2, body.size)

        ticketRepository.delete(ticket3)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket1)
        profileRepository.delete(customer)
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        profileRepository.delete(expert2)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getFilteredTicketsPriorityRange(){
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val customer2 = Profile()
        customer2.email = "mario.blu@polito.it"
        customer2.name = "Mario"
        customer2.surname = "Blu"
        customer2.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val expert2 = Profile()
        expert2.email = "luigi.bianchi@polito.it"
        expert2.name = "Luigi"
        expert2.surname = "Bianchi"
        expert2.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket1 = Ticket()
        ticket1.createdTimestamp = Timestamp(0)
        ticket1.product = product
        ticket1.customer = customer
        ticket1.status = TicketStatus.IN_PROGRESS
        ticket1.expert = expert
        ticket1.priority = 1
        ticket1.title = "Ticket sample"
        ticket1.description = "Ticket description sample"

        val ticket2 = Ticket()
        ticket2.createdTimestamp = Timestamp(1)
        ticket2.product = product
        ticket2.customer = customer
        ticket2.status = TicketStatus.IN_PROGRESS
        ticket2.expert = expert2
        ticket2.priority = 2
        ticket2.title = "Ticket sample"
        ticket2.description = "Ticket description sample"

        val ticket3 = Ticket()
        ticket3.createdTimestamp = Timestamp(2)
        ticket3.product = product
        ticket3.customer = customer2
        ticket3.status = TicketStatus.IN_PROGRESS
        ticket3.expert = expert
        ticket3.priority = 3
        ticket3.title = "Ticket sample"
        ticket3.description = "Ticket description sample"

        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val url = "http://localhost:$port/API/manager/ticketing/filter?minPriority=2&maxPriority=3"
        val uri = URI(url)
        val json = BasicJsonParser()

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

        Assertions.assertEquals(2, body.size)

        ticketRepository.delete(ticket3)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket1)
        profileRepository.delete(customer)
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        profileRepository.delete(expert2)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getFilteredTicketsProduct(){
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val customer2 = Profile()
        customer2.email = "mario.blu@polito.it"
        customer2.name = "Mario"
        customer2.surname = "Blu"
        customer2.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val expert2 = Profile()
        expert2.email = "luigi.bianchi@polito.it"
        expert2.name = "Luigi"
        expert2.surname = "Bianchi"
        expert2.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        val product2 = Product()
        product2.productId = "0000000000001"
        product2.name = "PC Omen Intel i5"
        product2.brand = "HP"

        productRepository.save(product)
        productRepository.save(product2)

        val ticket1 = Ticket()
        ticket1.createdTimestamp = Timestamp(0)
        ticket1.product = product
        ticket1.customer = customer
        ticket1.status = TicketStatus.IN_PROGRESS
        ticket1.expert = expert
        ticket1.priority = 1
        ticket1.title = "Ticket sample"
        ticket1.description = "Ticket description sample"

        val ticket2 = Ticket()
        ticket2.createdTimestamp = Timestamp(1)
        ticket2.product = product2
        ticket2.customer = customer
        ticket2.status = TicketStatus.IN_PROGRESS
        ticket2.expert = expert2
        ticket2.priority = 2
        ticket2.title = "Ticket sample"
        ticket2.description = "Ticket description sample"

        val ticket3 = Ticket()
        ticket3.createdTimestamp = Timestamp(2)
        ticket3.product = product2
        ticket3.customer = customer2
        ticket3.status = TicketStatus.IN_PROGRESS
        ticket3.expert = expert
        ticket3.priority = 3
        ticket3.title = "Ticket sample"
        ticket3.description = "Ticket description sample"

        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val url = "http://localhost:$port/API/manager/ticketing/filter?productId=0000000000001"
        val uri = URI(url)
        val json = BasicJsonParser()

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

        Assertions.assertEquals(2, body.size)

        ticketRepository.delete(ticket3)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket1)
        profileRepository.delete(customer)
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        profileRepository.delete(expert2)
        productRepository.delete(product)
        productRepository.delete(product2)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getFilteredTicketsStatus(){
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val customer2 = Profile()
        customer2.email = "mario.blu@polito.it"
        customer2.name = "Mario"
        customer2.surname = "Blu"
        customer2.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val expert2 = Profile()
        expert2.email = "luigi.bianchi@polito.it"
        expert2.name = "Luigi"
        expert2.surname = "Bianchi"
        expert2.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        val product2 = Product()
        product2.productId = "0000000000001"
        product2.name = "PC Omen Intel i5"
        product2.brand = "HP"

        productRepository.save(product)
        productRepository.save(product2)

        val ticket1 = Ticket()
        ticket1.createdTimestamp = Timestamp(0)
        ticket1.product = product
        ticket1.customer = customer
        ticket1.status = TicketStatus.OPEN
        ticket1.expert = expert
        ticket1.priority = 1
        ticket1.title = "Ticket sample"
        ticket1.description = "Ticket description sample"

        val ticket2 = Ticket()
        ticket2.createdTimestamp = Timestamp(1)
        ticket2.product = product2
        ticket2.customer = customer
        ticket2.status = TicketStatus.IN_PROGRESS
        ticket2.expert = expert2
        ticket2.priority = 2
        ticket2.title = "Ticket sample"
        ticket2.description = "Ticket description sample"

        val ticket3 = Ticket()
        ticket3.createdTimestamp = Timestamp(2)
        ticket3.product = product2
        ticket3.customer = customer2
        ticket3.status = TicketStatus.IN_PROGRESS
        ticket3.expert = expert
        ticket3.priority = 3
        ticket3.title = "Ticket sample"
        ticket3.description = "Ticket description sample"

        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val url = "http://localhost:$port/API/manager/ticketing/filter?status=IN_PROGRESS"
        val uri = URI(url)
        val json = BasicJsonParser()

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

        Assertions.assertEquals(2, body.size)

        ticketRepository.delete(ticket3)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket1)
        profileRepository.delete(customer)
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        profileRepository.delete(expert2)
        productRepository.delete(product)
        productRepository.delete(product2)
        profileRepository.delete(manager)
    }
    @Test
    @DirtiesContext
    fun getFilteredTicketsAllFilters(){
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val customer2 = Profile()
        customer2.email = "mario.blu@polito.it"
        customer2.name = "Mario"
        customer2.surname = "Blu"
        customer2.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val expert2 = Profile()
        expert2.email = "luigi.bianchi@polito.it"
        expert2.name = "Luigi"
        expert2.surname = "Bianchi"
        expert2.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        val product2 = Product()
        product2.productId = "0000000000001"
        product2.name = "PC Omen Intel i5"
        product2.brand = "HP"

        productRepository.save(product)
        productRepository.save(product2)

        val ticket1 = Ticket()
        ticket1.createdTimestamp = Timestamp(0)
        ticket1.product = product
        ticket1.customer = customer
        ticket1.status = TicketStatus.OPEN
        ticket1.expert = expert
        ticket1.priority = 1
        ticket1.title = "Ticket sample"
        ticket1.description = "Ticket description sample"

        val ticket2 = Ticket()
        ticket2.createdTimestamp = Timestamp(1)
        ticket2.product = product2
        ticket2.customer = customer
        ticket2.status = TicketStatus.IN_PROGRESS
        ticket2.expert = expert2
        ticket2.priority = 2
        ticket2.title = "Ticket sample"
        ticket2.description = "Ticket description sample"

        val ticket3 = Ticket()
        ticket3.createdTimestamp = Timestamp(2)
        ticket3.product = product2
        ticket3.customer = customer2
        ticket3.status = TicketStatus.IN_PROGRESS
        ticket3.expert = expert
        ticket3.priority = 3
        ticket3.title = "Ticket sample"
        ticket3.description = "Ticket description sample"

        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val url = "http://localhost:$port/API/manager/ticketing/filter?customerEmail=${customer.email}&expertEmail=${expert.email}&status=OPEN&productId=0000000000000&minPriority=1&maxPriority=2&createdAfter=${Timestamp(0).toLocalDateTime()}&createdBefore=${Timestamp(1).toLocalDateTime()}"
        val uri = URI(url)
        val json = BasicJsonParser()

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

        Assertions.assertEquals(1, body.size)

        ticketRepository.delete(ticket3)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket1)
        profileRepository.delete(customer)
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        profileRepository.delete(expert2)
        productRepository.delete(product)
        productRepository.delete(product2)
        profileRepository.delete(manager)
    }


    @Test
    @DirtiesContext
    fun addTicketSuccessfulClient(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = TicketDTO(
            null,
            "Ticket title",
            "Ticket description",
            null,
            "0000000000000",
            customer.email,
            expert.email,
            null,
            null
        )

        val url = "http://localhost:$port/API/client/ticketing/"
        val uri = URI(url)
        val json = BasicJsonParser()


        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)


        val requestEntity : HttpEntity<TicketDTO> = HttpEntity(ticket, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )


        Assertions.assertEquals(HttpStatus.CREATED, result.statusCode)

        val id = json.parseMap(result.body)["ticketId"] as Long

        val createdTicket = ticketRepository.findById(id)


        Assertions.assertTrue(createdTicket.isPresent)
        Assertions.assertEquals(ticket.title, createdTicket.get().title)
        Assertions.assertEquals(TicketStatus.OPEN, createdTicket.get().status)
        Assertions.assertEquals(ticket.description, createdTicket.get().description)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(createdTicket.get())
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(createdTicket.get())
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }



    @Test
    @DirtiesContext
    fun addTicketByExpertError(){
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = TicketDTO(
            null,
            "Ticket title",
            "Ticket description",
            null,
            "0000000000000",
            customer.email,
            expert.email,
            null,
            null
        )

        val url = "http://localhost:$port/API/client/ticketing/"
        val uri = URI(url)
        val json = BasicJsonParser()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(expertToken)


        val requestEntity : HttpEntity<TicketDTO> = HttpEntity(ticket, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun addTicketByManagerError(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = TicketDTO(
            null,
            "Ticket title",
            "Ticket description",
            null,
            "0000000000000",
            customer.email,
            expert.email,
            null,
            null
        )

        val url = "http://localhost:$port/API/client/ticketing/"
        val uri = URI(url)
        val json = BasicJsonParser()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)


        val requestEntity : HttpEntity<TicketDTO> = HttpEntity(ticket, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    /*@Test
    @DirtiesContext
    fun addTicketCustomerNotFound(){
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = TicketDTO(
            null,
            "Ticket title",
            "Ticket description",
            null,
            "0000000000000",
            "abc@mail.it",
            null,
            null,
            null
        )

        val url = "http://localhost:$port/API/ticketing/"
        val uri = URI(url)
        val json = BasicJsonParser()

        val requestEntity : HttpEntity<TicketDTO> = HttpEntity(ticket)
        val result = restTemplate.postForEntity(uri, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }*/

    @Test
    @DirtiesContext
    fun addTicketProductNotFound(){
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = TicketDTO(
            null,
            "Ticket title",
            "Ticket description",
            null,
            "0000000000001",
            customer.email,
            null,
            null,
            null
        )

        val url = "http://localhost:$port/API/client/ticketing/"
        val uri = URI(url)
        val json = BasicJsonParser()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)


        val requestEntity : HttpEntity<TicketDTO> = HttpEntity(ticket, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun addTicketProductWrongFormat(){
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = TicketDTO(
            null,
            "Ticket title",
            "Ticket description",
            null,
            "0000000000001abc",
            customer.email,
            null,
            null,
            null
        )

        val url = "http://localhost:$port/API/client/ticketing/"
        val uri = URI(url)
        val json = BasicJsonParser()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)


        val requestEntity : HttpEntity<TicketDTO> = HttpEntity(ticket, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun addTicketWithStatus(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = TicketDTO(
            null,
            "Ticket title",
            "Ticket description",
            null,
            "0000000000000",
            customer.email,
            null,
            TicketStatus.IN_PROGRESS,
            null
        )

        val url = "http://localhost:$port/API/client/ticketing/"
        val uri = URI(url)
        val json = BasicJsonParser()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)


        val requestEntity : HttpEntity<TicketDTO> = HttpEntity(ticket, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.CREATED, result.statusCode)

        val id = json.parseMap(result.body)["ticketId"] as Long

        val createdTicket = ticketRepository.findById(id)


        Assertions.assertTrue(createdTicket.isPresent)
        Assertions.assertEquals(ticket.title, createdTicket.get().title)
        Assertions.assertEquals(TicketStatus.OPEN, createdTicket.get().status)
        Assertions.assertEquals(ticket.description, createdTicket.get().description)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(createdTicket.get())
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(createdTicket.get())
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun assignTicketSuccessfulManager(){
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.OPEN
        ticketRepository.save(ticket)

        val ticketAssign = TicketAssignDTO(
            ticket.ticketId!!,
            expert.email!!,
            2
        )

        val url = "http://localhost:$port/API/manager/ticketing/assign"
        val uri = URI(url)
        val json = BasicJsonParser()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)


        val requestEntity : HttpEntity<TicketAssignDTO> = HttpEntity(ticketAssign, headers)

        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(ticket.title, updatedTicket.get().title)
        Assertions.assertEquals(TicketStatus.IN_PROGRESS, updatedTicket.get().status)
        Assertions.assertEquals(expert.email, updatedTicket.get().expert!!.email)
        Assertions.assertEquals(2, updatedTicket.get().priority)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun assignTicketForbiddenClient(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.OPEN
        ticketRepository.save(ticket)

        val ticketAssign = TicketAssignDTO(
            ticket.ticketId!!,
            expert.email!!,
            2
        )

        val url = "http://localhost:$port/API/manager/ticketing/assign"
        val uri = URI(url)
        val json = BasicJsonParser()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)


        val requestEntity : HttpEntity<TicketAssignDTO> = HttpEntity(ticketAssign, headers)

        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun assignTicketForbiddenExpert(){
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.OPEN
        ticketRepository.save(ticket)

        val ticketAssign = TicketAssignDTO(
            ticket.ticketId!!,
            expert.email!!,
            2
        )

        val url = "http://localhost:$port/API/manager/ticketing/assign"
        val uri = URI(url)
        val json = BasicJsonParser()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(expertToken)


        val requestEntity : HttpEntity<TicketAssignDTO> = HttpEntity(ticketAssign, headers)

        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun assignReopenedTicketSuccessfulManager(){
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.REOPENED
        ticketRepository.save(ticket)

        val ticketAssign = TicketAssignDTO(
            ticket.ticketId!!,
            expert.email!!,
            2
        )

        val url = "http://localhost:$port/API/manager/ticketing/assign"
        val uri = URI(url)
        val json = BasicJsonParser()


        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)
        val requestEntity : HttpEntity<TicketAssignDTO> = HttpEntity(ticketAssign, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(ticket.title, updatedTicket.get().title)
        Assertions.assertEquals(TicketStatus.IN_PROGRESS, updatedTicket.get().status)
        Assertions.assertEquals(expert.email, updatedTicket.get().expert!!.email)
        Assertions.assertEquals(2, updatedTicket.get().priority)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun assignTicketToCustomer(){
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.OPEN
        ticketRepository.save(ticket)

        val ticketAssign = TicketAssignDTO(
            ticket.ticketId!!,
            customer.email!!,
            2
        )

        val url = "http://localhost:$port/API/manager/ticketing/assign"
        val uri = URI(url)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketAssignDTO> = HttpEntity(ticketAssign, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun wrongReassignTicket(){
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.OPEN
        ticketRepository.save(ticket)

        val ticketAssign = TicketAssignDTO(
            ticket.ticketId!!,
            expert.email!!,
            2
        )

        val url = "http://localhost:$port/API/manager/ticketing/assign"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)
        val requestEntity : HttpEntity<TicketAssignDTO> = HttpEntity(ticketAssign, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)
        val result2 = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result2.statusCode)

        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun assignToClosedTicket(){
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.CLOSED
        ticketRepository.save(ticket)

        val ticketAssign = TicketAssignDTO(
            ticket.ticketId!!,
            expert.email!!,
            2
        )

        val url = "http://localhost:$port/API/manager/ticketing/assign"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketAssignDTO> = HttpEntity(ticketAssign, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulInProgressToClosedClient(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.IN_PROGRESS
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.CLOSED
        )

        val url = "http://localhost:$port/API/client/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(TicketStatus.CLOSED, updatedTicket.get().status)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulInProgressToClosedManager(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.IN_PROGRESS
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.CLOSED
        )

        val url = "http://localhost:$port/API/manager/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(TicketStatus.CLOSED, updatedTicket.get().status)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulInProgressToClosedExpert(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.IN_PROGRESS
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.CLOSED
        )

        val url = "http://localhost:$port/API/expert/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(expertToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        /*val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(TicketStatus.CLOSED, updatedTicket.get().status)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])*/
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulInProgressToOpenManager(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.IN_PROGRESS
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.OPEN
        )

        val url = "http://localhost:$port/API/manager/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(TicketStatus.OPEN, updatedTicket.get().status)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketForbiddenInProgressToOpenClient(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.IN_PROGRESS
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.OPEN
        )

        val url = "http://localhost:$port/API/client/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketForbiddenInProgressToOpenExpert(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.IN_PROGRESS
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.OPEN
        )

        val url = "http://localhost:$port/API/expert/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(expertToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulOpenToClosedClient(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.OPEN
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.CLOSED
        )

        val url = "http://localhost:$port/API/client/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(TicketStatus.CLOSED, updatedTicket.get().status)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulOpenToClosedManager(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.OPEN
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.CLOSED
        )

        val url = "http://localhost:$port/API/manager/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(TicketStatus.CLOSED, updatedTicket.get().status)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulOpenToClosedExpert(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.OPEN
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.CLOSED
        )

        val url = "http://localhost:$port/API/expert/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(expertToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        /*val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(TicketStatus.CLOSED, updatedTicket.get().status)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])*/
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulResolvedToClosedClient(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.RESOLVED
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.CLOSED
        )

        val url = "http://localhost:$port/API/client/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(TicketStatus.CLOSED, updatedTicket.get().status)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulResolvedToClosedManager(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.RESOLVED
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.CLOSED
        )

        val url = "http://localhost:$port/API/manager/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(TicketStatus.CLOSED, updatedTicket.get().status)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulResolvedToClosedExpert(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.RESOLVED
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.CLOSED
        )

        val url = "http://localhost:$port/API/expert/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(expertToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketForbiddenResolvedToClosedOtherClient(){
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val customer2 = Profile()
        customer2.email = "client@polito.it"
        customer2.name = "Mario"
        customer2.surname = "Rossi"
        customer2.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.RESOLVED
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.CLOSED
        )

        val url = "http://localhost:$port/API/client/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketForbiddenResolvedToClosedOtherExpert(){
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val expert2 = Profile()
        expert2.email = "expert@polito.it"
        expert.name = "Mario"
        expert2.surname = "Bianchi"
        expert2.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.RESOLVED
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.CLOSED
        )

        val url = "http://localhost:$port/API/expert/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(expertToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        profileRepository.delete(expert2)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulReopenedToClosedClient(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.REOPENED
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.CLOSED
        )

        val url = "http://localhost:$port/API/client/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(TicketStatus.CLOSED, updatedTicket.get().status)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulReopenedToClosedExpert(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.REOPENED
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.CLOSED
        )

        val url = "http://localhost:$port/API/expert/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(expertToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        /*val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(TicketStatus.CLOSED, updatedTicket.get().status)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])*/
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulReopenedToClosedManager(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.REOPENED
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.CLOSED
        )

        val url = "http://localhost:$port/API/manager/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(TicketStatus.CLOSED, updatedTicket.get().status)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulOpenToResolvedClient(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.OPEN
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.RESOLVED
        )

        val url = "http://localhost:$port/API/client/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(TicketStatus.RESOLVED, updatedTicket.get().status)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulOpenToResolvedManager(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.OPEN
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.RESOLVED
        )

        val url = "http://localhost:$port/API/manager/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(TicketStatus.RESOLVED, updatedTicket.get().status)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulOpenToResolvedExpert(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.OPEN
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.RESOLVED
        )

        val url = "http://localhost:$port/API/expert/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(expertToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        /*val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(TicketStatus.RESOLVED, updatedTicket.get().status)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])*/
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulInProgressToResolved(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.IN_PROGRESS
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.RESOLVED
        )

        val url = "http://localhost:$port/API/manager/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(TicketStatus.RESOLVED, updatedTicket.get().status)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulReopenedToResolved(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.REOPENED
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.RESOLVED
        )

        val url = "http://localhost:$port/API/manager/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(TicketStatus.RESOLVED, updatedTicket.get().status)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketWrongReopenedToInProgress(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.REOPENED
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.IN_PROGRESS
        )

        val url = "http://localhost:$port/API/manager/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulClosedToReopen(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.CLOSED
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.REOPENED
        )

        val url = "http://localhost:$port/API/manager/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(TicketStatus.REOPENED, updatedTicket.get().status)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulResolvedToReopen(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.RESOLVED
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.REOPENED
        )

        val url = "http://localhost:$port/API/manager/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.ticketId!!)

        Assertions.assertTrue(updatedTicket.isPresent)
        Assertions.assertEquals(TicketStatus.REOPENED, updatedTicket.get().status)


        val createdTicketHistory = ticketHistoryRepository.findAllByTicket(ticket)
        ticketHistoryRepository.delete(createdTicketHistory[0])
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketWrongOpenToReopened(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.OPEN
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.REOPENED
        )

        val url = "http://localhost:$port/API/manager/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketWrongClosedToOpen(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.CLOSED
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.OPEN
        )

        val url = "http://localhost:$port/API/manager/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketWrongClosedToInProgress(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.CLOSED
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.IN_PROGRESS
        )

        val url = "http://localhost:$port/API/manager/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketWrongClosedToResolved(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.CLOSED
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.RESOLVED
        )

        val url = "http://localhost:$port/API/manager/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketWrongInProgressToReopened(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.IN_PROGRESS
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.REOPENED
        )

        val url = "http://localhost:$port/API/manager/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketWrongReopenedToOpen(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.REOPENED
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.OPEN
        )

        val url = "http://localhost:$port/API/manager/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketWrongResolvedToInProgress(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.RESOLVED
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.IN_PROGRESS
        )

        val url = "http://localhost:$port/API/manager/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun updateTicketWrongResolvedToOpen(){
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER


        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()

        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"
        productRepository.save(product)

        val ticket = Ticket()
        ticket.product = product
        ticket.title = "Ticket title"
        ticket.description = "Ticket description"
        ticket.customer = customer
        ticket.createdTimestamp = Timestamp(0)
        ticket.status = TicketStatus.RESOLVED
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.ticketId!!,
            TicketStatus.OPEN
        )

        val url = "http://localhost:$port/API/manager/ticketing/update"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate, headers)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }
}

