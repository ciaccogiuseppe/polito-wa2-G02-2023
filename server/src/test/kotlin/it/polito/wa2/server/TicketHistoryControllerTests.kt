package it.polito.wa2.server

import dasniko.testcontainers.keycloak.KeycloakContainer
import it.polito.wa2.server.products.Product
import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileRole
import it.polito.wa2.server.ticketing.ticket.Ticket
import it.polito.wa2.server.ticketing.ticket.TicketRepository
import it.polito.wa2.server.ticketing.ticket.TicketStatus
import it.polito.wa2.server.ticketing.tickethistory.TicketHistory
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

@Testcontainers
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
class TicketHistoryControllerTests {
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

    // --------------------------- no filters

    @Test
    @DirtiesContext
    fun getTicketHistoryWithNoFiltersAtAll() {
        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        val url = "http://localhost:$port/API/ticketing/history/filter"
        val uri = URI(url)

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


        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryWithNoFiltersAtAllForbiddenClient() {
        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        val url = "http://localhost:$port/API/ticketing/history/filter"
        val uri = URI(url)

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
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryWithNoFiltersAtAllForbiddenExpert() {
        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        val url = "http://localhost:$port/API/ticketing/history/filter"
        val uri = URI(url)

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
        profileRepository.delete(manager)
    }

    // --------------------------- ticketId

    @Test
    @DirtiesContext
    fun getExistingTicketHistoryByTicketIdEmpty() {
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
        ticket2.expert = expert
        ticket2.priority = 2
        ticket2.title = "Ticket sample"
        ticket2.description = "Ticket description sample"

        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)

        val history1ticket1 = TicketHistory()
        history1ticket1.ticket = ticket1
        history1ticket1.currentExpert = expert
        history1ticket1.newState = TicketStatus.CLOSED
        history1ticket1.oldState = TicketStatus.IN_PROGRESS
        history1ticket1.updatedTimestamp = Timestamp(34)
        history1ticket1.user = customer

        ticketHistoryRepository.save(history1ticket1)

        val url = "http://localhost:$port/API/ticketing/history/filter?ticketId=2"
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

        Assertions.assertEquals(body.size, 0)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        ticketHistoryRepository.delete(history1ticket1)
        ticketRepository.delete(ticket1)
        ticketRepository.delete(ticket2)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getExistingTicketHistoryByTicketId() {
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
        ticket2.expert = expert
        ticket2.priority = 2
        ticket2.title = "Ticket sample"
        ticket2.description = "Ticket description sample"

        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)

        val history1ticket1 = TicketHistory()
        history1ticket1.ticket = ticket1
        history1ticket1.currentExpert = expert
        history1ticket1.newState = TicketStatus.CLOSED
        history1ticket1.oldState = TicketStatus.IN_PROGRESS
        history1ticket1.updatedTimestamp = Timestamp(34)
        history1ticket1.user = customer

        val history2ticket2 = TicketHistory()
        history2ticket2.ticket = ticket2
        history2ticket2.currentExpert = expert
        history2ticket2.newState = TicketStatus.RESOLVED
        history2ticket2.oldState = TicketStatus.OPEN
        history2ticket2.updatedTimestamp = Timestamp(19)
        history2ticket2.user = customer

        val history3ticket2 = TicketHistory()
        history3ticket2.ticket = ticket2
        history3ticket2.currentExpert = expert
        history3ticket2.newState = TicketStatus.REOPENED
        history3ticket2.oldState = TicketStatus.RESOLVED
        history3ticket2.updatedTimestamp = Timestamp(122)
        history3ticket2.user = customer

        ticketHistoryRepository.save(history1ticket1)
        ticketHistoryRepository.save(history2ticket2)
        ticketHistoryRepository.save(history3ticket2)

        val url = "http://localhost:$port/API/ticketing/history/filter?ticketId=2"
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

        Assertions.assertEquals(body.size, 2)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(body[0]["historyId"], history2ticket2.historyId)
        Assertions.assertEquals(body[0]["ticketId"], history2ticket2.ticket!!.ticketId)
        Assertions.assertEquals(body[0]["newState"], history2ticket2.newState.name)
        Assertions.assertEquals(body[0]["oldState"], history2ticket2.oldState.name)
        Assertions.assertEquals(body[0]["currentExpertEmail"], history2ticket2.currentExpert!!.email)
        Assertions.assertEquals(body[0]["userEmail"], history2ticket2.user!!.email)
        Assertions.assertTrue(body[0]["updatedTimestamp"].toString().startsWith(history2ticket2.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        Assertions.assertEquals(body[1]["historyId"], history3ticket2.historyId)
        Assertions.assertEquals(body[1]["ticketId"], history3ticket2.ticket!!.ticketId)
        Assertions.assertEquals(body[1]["newState"], history3ticket2.newState.name)
        Assertions.assertEquals(body[1]["oldState"], history3ticket2.oldState.name)
        Assertions.assertEquals(body[1]["currentExpertEmail"], history3ticket2.currentExpert!!.email)
        Assertions.assertEquals(body[1]["userEmail"], history3ticket2.user!!.email)
        Assertions.assertTrue(body[1]["updatedTimestamp"].toString().startsWith(history3ticket2.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        ticketHistoryRepository.delete(history1ticket1)
        ticketHistoryRepository.delete(history2ticket2)
        ticketHistoryRepository.delete(history3ticket2)
        ticketRepository.delete(ticket1)
        ticketRepository.delete(ticket2)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getNonExistingTicketHistoryByTicketId() {
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

        val ticket1 = Ticket()
        ticket1.createdTimestamp = Timestamp(0)
        ticket1.product = product
        ticket1.customer = customer
        ticket1.status = TicketStatus.IN_PROGRESS
        ticket1.expert = expert
        ticket1.priority = 2
        ticket1.title = "Ticket sample"
        ticket1.description = "Ticket description sample"

        ticketRepository.save(ticket1)

        val history1ticket1 = TicketHistory()
        history1ticket1.ticket = ticket1
        history1ticket1.currentExpert = expert
        history1ticket1.newState = TicketStatus.CLOSED
        history1ticket1.oldState = TicketStatus.IN_PROGRESS
        history1ticket1.updatedTimestamp = Timestamp(34)
        history1ticket1.user = customer

        ticketHistoryRepository.save(history1ticket1)

        val url = "http://localhost:$port/API/ticketing/history/filter?ticketId=2"
        val uri = URI(url)

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

        ticketHistoryRepository.delete(history1ticket1)
        ticketRepository.delete(ticket1)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryByTicketIdNegative() {
        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        val url = "http://localhost:$port/API/ticketing/history/filter?ticketId=-3"
        val uri = URI(url)

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
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryByTicketIdAlphabetic() {
        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        val url = "http://localhost:$port/API/ticketing/history/filter?ticketId=a"
        val uri = URI(url)

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

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
        profileRepository.delete(manager)
    }

    // --------------------------- userEmail

    @Test
    @DirtiesContext
    fun getExistingTicketHistoryByUserEmail() {
        val customer1 = Profile()
        customer1.email = "mario.rossi@polito.it"
        customer1.name = "Mario"
        customer1.surname = "Rossi"
        customer1.role = ProfileRole.CUSTOMER

        val customer2 = Profile()
        customer2.email = "luigi.verdi@polito.it"
        customer2.name = "Luigi"
        customer2.surname = "Verdi"
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
        profileRepository.save(customer1)
        profileRepository.save(customer2)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket = Ticket()
        ticket.createdTimestamp = Timestamp(0)
        ticket.product = product
        ticket.customer = customer1
        ticket.status = TicketStatus.IN_PROGRESS
        ticket.expert = expert
        ticket.priority = 2
        ticket.title = "Ticket sample"
        ticket.description = "Ticket description sample"

        ticketRepository.save(ticket)

        val history1customer1 = TicketHistory()
        history1customer1.ticket = ticket
        history1customer1.currentExpert = expert
        history1customer1.newState = TicketStatus.CLOSED
        history1customer1.oldState = TicketStatus.IN_PROGRESS
        history1customer1.updatedTimestamp = Timestamp(34)
        history1customer1.user = customer1

        val history2customer2 = TicketHistory()
        history2customer2.ticket = ticket
        history2customer2.currentExpert = expert
        history2customer2.newState = TicketStatus.RESOLVED
        history2customer2.oldState = TicketStatus.OPEN
        history2customer2.updatedTimestamp = Timestamp(19)
        history2customer2.user = customer2

        val history3customer2 = TicketHistory()
        history3customer2.ticket = ticket
        history3customer2.currentExpert = expert
        history3customer2.newState = TicketStatus.REOPENED
        history3customer2.oldState = TicketStatus.RESOLVED
        history3customer2.updatedTimestamp = Timestamp(122)
        history3customer2.user = customer2

        ticketHistoryRepository.save(history1customer1)
        ticketHistoryRepository.save(history2customer2)
        ticketHistoryRepository.save(history3customer2)

        val url = "http://localhost:$port/API/ticketing/history/filter?userEmail=${customer2.email}"
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

        Assertions.assertEquals(body.size, 2)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(body[0]["historyId"], history2customer2.historyId)
        Assertions.assertEquals(body[0]["ticketId"], history2customer2.ticket!!.ticketId)
        Assertions.assertEquals(body[0]["newState"], history2customer2.newState.name)
        Assertions.assertEquals(body[0]["oldState"], history2customer2.oldState.name)
        Assertions.assertEquals(body[0]["currentExpertEmail"], history2customer2.currentExpert!!.email)
        Assertions.assertEquals(body[0]["userEmail"], history2customer2.user!!.email)
        Assertions.assertTrue(body[0]["updatedTimestamp"].toString().startsWith(history2customer2.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        Assertions.assertEquals(body[1]["historyId"], history3customer2.historyId)
        Assertions.assertEquals(body[1]["ticketId"], history3customer2.ticket!!.ticketId)
        Assertions.assertEquals(body[1]["newState"], history3customer2.newState.name)
        Assertions.assertEquals(body[1]["oldState"], history3customer2.oldState.name)
        Assertions.assertEquals(body[1]["currentExpertEmail"], history3customer2.currentExpert!!.email)
        Assertions.assertEquals(body[1]["userEmail"], history3customer2.user!!.email)
        Assertions.assertTrue(body[1]["updatedTimestamp"].toString().startsWith(history3customer2.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        ticketHistoryRepository.delete(history1customer1)
        ticketHistoryRepository.delete(history2customer2)
        ticketHistoryRepository.delete(history3customer2)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer1)
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getNonExistingTicketHistoryByUserEmail() {
        val customer1 = Profile()
        customer1.email = "mario.rossi@polito.it"
        customer1.name = "Mario"
        customer1.surname = "Rossi"
        customer1.role = ProfileRole.CUSTOMER

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
        profileRepository.save(customer1)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket = Ticket()
        ticket.createdTimestamp = Timestamp(0)
        ticket.product = product
        ticket.customer = customer1
        ticket.status = TicketStatus.IN_PROGRESS
        ticket.expert = expert
        ticket.priority = 2
        ticket.title = "Ticket sample"
        ticket.description = "Ticket description sample"

        ticketRepository.save(ticket)

        val history1customer1 = TicketHistory()
        history1customer1.ticket = ticket
        history1customer1.currentExpert = expert
        history1customer1.newState = TicketStatus.CLOSED
        history1customer1.oldState = TicketStatus.IN_PROGRESS
        history1customer1.updatedTimestamp = Timestamp(34)
        history1customer1.user = customer1

        ticketHistoryRepository.save(history1customer1)

        val url = "http://localhost:$port/API/ticketing/history/filter?userEmail=not.found@polito.it"
        val uri = URI(url)

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


        ticketHistoryRepository.delete(history1customer1)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer1)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryByUserEmailInvalid() {
        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        val url = "http://localhost:$port/API/ticketing/history/filter?userEmail=invalidEmail"
        val uri = URI(url)

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
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryByUserEmailNumeric() {
        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        val url = "http://localhost:$port/API/ticketing/history/filter?userEmail=33"
        val uri = URI(url)

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
        profileRepository.delete(manager)
    }

    // --------------------------- currentExpertEmail

    @Test
    @DirtiesContext
    fun getExistingTicketHistoryByCurrentExpertEmail() {
        val customer1 = Profile()
        customer1.email = "mario.rossi@polito.it"
        customer1.name = "Mario"
        customer1.surname = "Rossi"
        customer1.role = ProfileRole.CUSTOMER

        val expert2 = Profile()
        expert2.email = "luigi.verdi@polito.it"
        expert2.name = "Luigi"
        expert2.surname = "Verdi"
        expert2.role = ProfileRole.EXPERT

        val expert3 = Profile()
        expert3.email = "mario.bianchi@polito.it"
        expert3.name = "Mario"
        expert3.surname = "Bianchi"
        expert3.role = ProfileRole.EXPERT


        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer1)
        profileRepository.save(expert2)
        profileRepository.save(expert3)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket = Ticket()
        ticket.createdTimestamp = Timestamp(0)
        ticket.product = product
        ticket.customer = customer1
        ticket.status = TicketStatus.IN_PROGRESS
        ticket.expert = expert3
        ticket.priority = 2
        ticket.title = "Ticket sample"
        ticket.description = "Ticket description sample"

        ticketRepository.save(ticket)

        val history1expert2 = TicketHistory()
        history1expert2.ticket = ticket
        history1expert2.currentExpert = expert2
        history1expert2.newState = TicketStatus.CLOSED
        history1expert2.oldState = TicketStatus.IN_PROGRESS
        history1expert2.updatedTimestamp = Timestamp(34)
        history1expert2.user = customer1

        val history2expert3 = TicketHistory()
        history2expert3.ticket = ticket
        history2expert3.currentExpert = expert3
        history2expert3.newState = TicketStatus.RESOLVED
        history2expert3.oldState = TicketStatus.OPEN
        history2expert3.updatedTimestamp = Timestamp(19)
        history2expert3.user = customer1

        val history3expert3 = TicketHistory()
        history3expert3.ticket = ticket
        history3expert3.currentExpert = expert3
        history3expert3.newState = TicketStatus.REOPENED
        history3expert3.oldState = TicketStatus.RESOLVED
        history3expert3.updatedTimestamp = Timestamp(122)
        history3expert3.user = customer1

        ticketHistoryRepository.save(history1expert2)
        ticketHistoryRepository.save(history2expert3)
        ticketHistoryRepository.save(history3expert3)

        val url = "http://localhost:$port/API/ticketing/history/filter?currentExpertEmail=${expert3.email}"
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

        Assertions.assertEquals(body.size, 2)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(body[0]["historyId"], history2expert3.historyId)
        Assertions.assertEquals(body[0]["ticketId"], history2expert3.ticket!!.ticketId)
        Assertions.assertEquals(body[0]["newState"], history2expert3.newState.name)
        Assertions.assertEquals(body[0]["oldState"], history2expert3.oldState.name)
        Assertions.assertEquals(body[0]["currentExpertEmail"], history2expert3.currentExpert!!.email)
        Assertions.assertEquals(body[0]["userEmail"], history2expert3.user!!.email)
        Assertions.assertTrue(body[0]["updatedTimestamp"].toString().startsWith(history2expert3.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        Assertions.assertEquals(body[1]["historyId"], history3expert3.historyId)
        Assertions.assertEquals(body[1]["ticketId"], history3expert3.ticket!!.ticketId)
        Assertions.assertEquals(body[1]["newState"], history3expert3.newState.name)
        Assertions.assertEquals(body[1]["oldState"], history3expert3.oldState.name)
        Assertions.assertEquals(body[1]["currentExpertEmail"], history3expert3.currentExpert!!.email)
        Assertions.assertEquals(body[1]["userEmail"], history3expert3.user!!.email)
        Assertions.assertTrue(body[1]["updatedTimestamp"].toString().startsWith(history3expert3.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        ticketHistoryRepository.delete(history1expert2)
        ticketHistoryRepository.delete(history2expert3)
        ticketHistoryRepository.delete(history3expert3)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer1)
        profileRepository.delete(expert2)
        profileRepository.delete(expert3)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getNonExistingTicketHistoryByCurrentExpertEmail() {
        val customer1 = Profile()
        customer1.email = "mario.rossi@polito.it"
        customer1.name = "Mario"
        customer1.surname = "Rossi"
        customer1.role = ProfileRole.CUSTOMER

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
        profileRepository.save(customer1)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket = Ticket()
        ticket.createdTimestamp = Timestamp(0)
        ticket.product = product
        ticket.customer = customer1
        ticket.status = TicketStatus.IN_PROGRESS
        ticket.expert = expert
        ticket.priority = 2
        ticket.title = "Ticket sample"
        ticket.description = "Ticket description sample"

        ticketRepository.save(ticket)

        val history1customer1 = TicketHistory()
        history1customer1.ticket = ticket
        history1customer1.currentExpert = expert
        history1customer1.newState = TicketStatus.CLOSED
        history1customer1.oldState = TicketStatus.IN_PROGRESS
        history1customer1.updatedTimestamp = Timestamp(34)
        history1customer1.user = customer1

        ticketHistoryRepository.save(history1customer1)

        val url = "http://localhost:$port/API/ticketing/history/filter?currentExpertEmail=not.found@polito.it"
        val uri = URI(url)

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


        ticketHistoryRepository.delete(history1customer1)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer1)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryByCurrentExpertEmailInvalid() {
        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        val url = "http://localhost:$port/API/ticketing/history/filter?currentExpertEmail=invalidEmail"
        val uri = URI(url)

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
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryByCurrentExpertEmailNumeric() {
        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        val url = "http://localhost:$port/API/ticketing/history/filter?currentExpertEmail=33"
        val uri = URI(url)

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
        profileRepository.delete(manager)
    }

    // --------------------------- updatedAfter & updatedBefore

    @Test
    @DirtiesContext
    fun getTicketHistoryByUpdatedAfter() {
        val customer1 = Profile()
        customer1.email = "mario.rossi@polito.it"
        customer1.name = "Mario"
        customer1.surname = "Rossi"
        customer1.role = ProfileRole.CUSTOMER

        val expert2 = Profile()
        expert2.email = "luigi.verdi@polito.it"
        expert2.name = "Luigi"
        expert2.surname = "Verdi"
        expert2.role = ProfileRole.EXPERT

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer1)
        profileRepository.save(expert2)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket = Ticket()
        ticket.createdTimestamp = Timestamp(0)
        ticket.product = product
        ticket.customer = customer1
        ticket.status = TicketStatus.IN_PROGRESS
        ticket.expert = expert2
        ticket.priority = 2
        ticket.title = "Ticket sample"
        ticket.description = "Ticket description sample"

        ticketRepository.save(ticket)

        val history1timestamp10 = TicketHistory()
        history1timestamp10.ticket = ticket
        history1timestamp10.currentExpert = expert2
        history1timestamp10.newState = TicketStatus.CLOSED
        history1timestamp10.oldState = TicketStatus.IN_PROGRESS
        history1timestamp10.updatedTimestamp = Timestamp(10)
        history1timestamp10.user = customer1

        val history2timestamp20 = TicketHistory()
        history2timestamp20.ticket = ticket
        history2timestamp20.currentExpert = expert2
        history2timestamp20.newState = TicketStatus.RESOLVED
        history2timestamp20.oldState = TicketStatus.OPEN
        history2timestamp20.updatedTimestamp = Timestamp(20)
        history2timestamp20.user = customer1

        val history3timestamp30 = TicketHistory()
        history3timestamp30.ticket = ticket
        history3timestamp30.currentExpert = expert2
        history3timestamp30.newState = TicketStatus.REOPENED
        history3timestamp30.oldState = TicketStatus.RESOLVED
        history3timestamp30.updatedTimestamp = Timestamp(30)
        history3timestamp30.user = customer1

        ticketHistoryRepository.save(history1timestamp10)
        ticketHistoryRepository.save(history2timestamp20)
        ticketHistoryRepository.save(history3timestamp30)

        val url = "http://localhost:$port/API/ticketing/history/filter?updatedAfter=${Timestamp(20).toLocalDateTime()}"
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

        Assertions.assertEquals(body.size, 2)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(body[0]["historyId"], history2timestamp20.historyId)
        Assertions.assertEquals(body[0]["ticketId"], history2timestamp20.ticket!!.ticketId)
        Assertions.assertEquals(body[0]["newState"], history2timestamp20.newState.name)
        Assertions.assertEquals(body[0]["oldState"], history2timestamp20.oldState.name)
        Assertions.assertEquals(body[0]["currentExpertEmail"], history2timestamp20.currentExpert!!.email)
        Assertions.assertEquals(body[0]["userEmail"], history2timestamp20.user!!.email)
        Assertions.assertTrue(body[0]["updatedTimestamp"].toString().startsWith(history2timestamp20.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        Assertions.assertEquals(body[1]["historyId"], history3timestamp30.historyId)
        Assertions.assertEquals(body[1]["ticketId"], history3timestamp30.ticket!!.ticketId)
        Assertions.assertEquals(body[1]["newState"], history3timestamp30.newState.name)
        Assertions.assertEquals(body[1]["oldState"], history3timestamp30.oldState.name)
        Assertions.assertEquals(body[1]["currentExpertEmail"], history3timestamp30.currentExpert!!.email)
        Assertions.assertEquals(body[1]["userEmail"], history3timestamp30.user!!.email)
        Assertions.assertTrue(body[1]["updatedTimestamp"].toString().startsWith(history3timestamp30.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        ticketHistoryRepository.delete(history1timestamp10)
        ticketHistoryRepository.delete(history2timestamp20)
        ticketHistoryRepository.delete(history3timestamp30)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer1)
        profileRepository.delete(expert2)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryByUpdatedBefore() {
        val customer1 = Profile()
        customer1.email = "mario.rossi@polito.it"
        customer1.name = "Mario"
        customer1.surname = "Rossi"
        customer1.role = ProfileRole.CUSTOMER

        val expert2 = Profile()
        expert2.email = "luigi.verdi@polito.it"
        expert2.name = "Luigi"
        expert2.surname = "Verdi"
        expert2.role = ProfileRole.EXPERT

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer1)
        profileRepository.save(expert2)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket = Ticket()
        ticket.createdTimestamp = Timestamp(0)
        ticket.product = product
        ticket.customer = customer1
        ticket.status = TicketStatus.IN_PROGRESS
        ticket.expert = expert2
        ticket.priority = 2
        ticket.title = "Ticket sample"
        ticket.description = "Ticket description sample"

        ticketRepository.save(ticket)

        val history1timestamp10 = TicketHistory()
        history1timestamp10.ticket = ticket
        history1timestamp10.currentExpert = expert2
        history1timestamp10.newState = TicketStatus.CLOSED
        history1timestamp10.oldState = TicketStatus.IN_PROGRESS
        history1timestamp10.updatedTimestamp = Timestamp(10)
        history1timestamp10.user = customer1

        val history2timestamp20 = TicketHistory()
        history2timestamp20.ticket = ticket
        history2timestamp20.currentExpert = expert2
        history2timestamp20.newState = TicketStatus.RESOLVED
        history2timestamp20.oldState = TicketStatus.OPEN
        history2timestamp20.updatedTimestamp = Timestamp(20)
        history2timestamp20.user = customer1

        val history3timestamp30 = TicketHistory()
        history3timestamp30.ticket = ticket
        history3timestamp30.currentExpert = expert2
        history3timestamp30.newState = TicketStatus.REOPENED
        history3timestamp30.oldState = TicketStatus.RESOLVED
        history3timestamp30.updatedTimestamp = Timestamp(30)
        history3timestamp30.user = customer1

        ticketHistoryRepository.save(history1timestamp10)
        ticketHistoryRepository.save(history2timestamp20)
        ticketHistoryRepository.save(history3timestamp30)

        val url = "http://localhost:$port/API/ticketing/history/filter?updatedBefore=${Timestamp(19).toLocalDateTime()}"
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

        Assertions.assertEquals(body.size, 1)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(body[0]["historyId"], history1timestamp10.historyId)
        Assertions.assertEquals(body[0]["ticketId"], history1timestamp10.ticket!!.ticketId)
        Assertions.assertEquals(body[0]["newState"], history1timestamp10.newState.name)
        Assertions.assertEquals(body[0]["oldState"], history1timestamp10.oldState.name)
        Assertions.assertEquals(body[0]["currentExpertEmail"], history1timestamp10.currentExpert!!.email)
        Assertions.assertEquals(body[0]["userEmail"], history1timestamp10.user!!.email)
        Assertions.assertTrue(body[0]["updatedTimestamp"].toString().startsWith(history1timestamp10.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        ticketHistoryRepository.delete(history1timestamp10)
        ticketHistoryRepository.delete(history2timestamp20)
        ticketHistoryRepository.delete(history3timestamp30)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer1)
        profileRepository.delete(expert2)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryByUpdatedAfterAndUpdatedBefore() {
        val customer1 = Profile()
        customer1.email = "mario.rossi@polito.it"
        customer1.name = "Mario"
        customer1.surname = "Rossi"
        customer1.role = ProfileRole.CUSTOMER

        val expert2 = Profile()
        expert2.email = "luigi.verdi@polito.it"
        expert2.name = "Luigi"
        expert2.surname = "Verdi"
        expert2.role = ProfileRole.EXPERT

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer1)
        profileRepository.save(expert2)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket = Ticket()
        ticket.createdTimestamp = Timestamp(0)
        ticket.product = product
        ticket.customer = customer1
        ticket.status = TicketStatus.IN_PROGRESS
        ticket.expert = expert2
        ticket.priority = 2
        ticket.title = "Ticket sample"
        ticket.description = "Ticket description sample"

        ticketRepository.save(ticket)

        val history1timestamp10 = TicketHistory()
        history1timestamp10.ticket = ticket
        history1timestamp10.currentExpert = expert2
        history1timestamp10.newState = TicketStatus.CLOSED
        history1timestamp10.oldState = TicketStatus.IN_PROGRESS
        history1timestamp10.updatedTimestamp = Timestamp(10)
        history1timestamp10.user = customer1

        val history2timestamp20 = TicketHistory()
        history2timestamp20.ticket = ticket
        history2timestamp20.currentExpert = expert2
        history2timestamp20.newState = TicketStatus.RESOLVED
        history2timestamp20.oldState = TicketStatus.OPEN
        history2timestamp20.updatedTimestamp = Timestamp(20)
        history2timestamp20.user = customer1

        val history3timestamp30 = TicketHistory()
        history3timestamp30.ticket = ticket
        history3timestamp30.currentExpert = expert2
        history3timestamp30.newState = TicketStatus.REOPENED
        history3timestamp30.oldState = TicketStatus.RESOLVED
        history3timestamp30.updatedTimestamp = Timestamp(30)
        history3timestamp30.user = customer1

        ticketHistoryRepository.save(history1timestamp10)
        ticketHistoryRepository.save(history2timestamp20)
        ticketHistoryRepository.save(history3timestamp30)

        val url = "http://localhost:$port/API/ticketing/history/filter?updatedAfter=${Timestamp(9).toLocalDateTime()}&updatedBefore=${Timestamp(10).toLocalDateTime()}"
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

        Assertions.assertEquals(body.size, 1)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(body[0]["historyId"], history1timestamp10.historyId)
        Assertions.assertEquals(body[0]["ticketId"], history1timestamp10.ticket!!.ticketId)
        Assertions.assertEquals(body[0]["newState"], history1timestamp10.newState.name)
        Assertions.assertEquals(body[0]["oldState"], history1timestamp10.oldState.name)
        Assertions.assertEquals(body[0]["currentExpertEmail"], history1timestamp10.currentExpert!!.email)
        Assertions.assertEquals(body[0]["userEmail"], history1timestamp10.user!!.email)
        Assertions.assertTrue(body[0]["updatedTimestamp"].toString().startsWith(history1timestamp10.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        ticketHistoryRepository.delete(history1timestamp10)
        ticketHistoryRepository.delete(history2timestamp20)
        ticketHistoryRepository.delete(history3timestamp30)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer1)
        profileRepository.delete(expert2)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryByUpdatedAfterAndUpdatedBeforeEmpty() {
        val customer1 = Profile()
        customer1.email = "mario.rossi@polito.it"
        customer1.name = "Mario"
        customer1.surname = "Rossi"
        customer1.role = ProfileRole.CUSTOMER

        val expert2 = Profile()
        expert2.email = "luigi.verdi@polito.it"
        expert2.name = "Luigi"
        expert2.surname = "Verdi"
        expert2.role = ProfileRole.EXPERT

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer1)
        profileRepository.save(expert2)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket = Ticket()
        ticket.createdTimestamp = Timestamp(0)
        ticket.product = product
        ticket.customer = customer1
        ticket.status = TicketStatus.IN_PROGRESS
        ticket.expert = expert2
        ticket.priority = 2
        ticket.title = "Ticket sample"
        ticket.description = "Ticket description sample"

        ticketRepository.save(ticket)

        val history1timestamp10 = TicketHistory()
        history1timestamp10.ticket = ticket
        history1timestamp10.currentExpert = expert2
        history1timestamp10.newState = TicketStatus.CLOSED
        history1timestamp10.oldState = TicketStatus.IN_PROGRESS
        history1timestamp10.updatedTimestamp = Timestamp(10)
        history1timestamp10.user = customer1

        val history2timestamp20 = TicketHistory()
        history2timestamp20.ticket = ticket
        history2timestamp20.currentExpert = expert2
        history2timestamp20.newState = TicketStatus.RESOLVED
        history2timestamp20.oldState = TicketStatus.OPEN
        history2timestamp20.updatedTimestamp = Timestamp(20)
        history2timestamp20.user = customer1

        val history3timestamp30 = TicketHistory()
        history3timestamp30.ticket = ticket
        history3timestamp30.currentExpert = expert2
        history3timestamp30.newState = TicketStatus.REOPENED
        history3timestamp30.oldState = TicketStatus.RESOLVED
        history3timestamp30.updatedTimestamp = Timestamp(30)
        history3timestamp30.user = customer1

        ticketHistoryRepository.save(history1timestamp10)
        ticketHistoryRepository.save(history2timestamp20)
        ticketHistoryRepository.save(history3timestamp30)

        val url = "http://localhost:$port/API/ticketing/history/filter?updatedAfter=${Timestamp(11).toLocalDateTime()}&updatedBefore=${Timestamp(19).toLocalDateTime()}"
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

        Assertions.assertEquals(body.size, 0)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        ticketHistoryRepository.delete(history1timestamp10)
        ticketHistoryRepository.delete(history2timestamp20)
        ticketHistoryRepository.delete(history3timestamp30)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer1)
        profileRepository.delete(expert2)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryByUpdatedAfterAndUpdatedBeforeUnprocessable() {
        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        val url = "http://localhost:$port/API/ticketing/history/filter?updatedAfter=${Timestamp(11).toLocalDateTime()}&updatedBefore=${Timestamp(9).toLocalDateTime()}"
        val uri = URI(url)

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
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryByUpdatedAfterAndUpdatedBeforeEqual() {
        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        val url = "http://localhost:$port/API/ticketing/history/filter?updatedAfter=${Timestamp(10).toLocalDateTime()}&updatedBefore=${Timestamp(10).toLocalDateTime()}"
        val uri = URI(url)

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

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
    }

    // --------------------------- all filters

    @Test
    @DirtiesContext
    fun getTicketHistoryAllFilters() {
        val customer1 = Profile()
        customer1.email = "mario.rossi@polito.it"
        customer1.name = "Mario"
        customer1.surname = "Rossi"
        customer1.role = ProfileRole.CUSTOMER

        val customer2 = Profile()
        customer2.email = "luigi.verdi@polito.it"
        customer2.name = "Luigi"
        customer2.surname = "Verdi"
        customer2.role = ProfileRole.CUSTOMER

        val expert3 = Profile()
        expert3.email = "mario.bianchi@polito.it"
        expert3.name = "Mario"
        expert3.surname = "Bianchi"
        expert3.role = ProfileRole.EXPERT

        val expert4 = Profile()
        expert4.email = "luigi.viola@polito.it"
        expert4.name = "Luigi"
        expert4.surname = "Viola"
        expert4.role = ProfileRole.EXPERT

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
        profileRepository.save(customer1)
        profileRepository.save(customer2)
        profileRepository.save(expert3)
        profileRepository.save(expert4)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)

        val ticket1 = Ticket()
        ticket1.createdTimestamp = Timestamp(0)
        ticket1.product = product
        ticket1.customer = customer1
        ticket1.status = TicketStatus.IN_PROGRESS
        ticket1.expert = expert3
        ticket1.priority = 2
        ticket1.title = "Ticket sample"
        ticket1.description = "Ticket description sample"

        val ticket2 = Ticket()
        ticket2.createdTimestamp = Timestamp(0)
        ticket2.product = product
        ticket2.customer = customer2
        ticket2.status = TicketStatus.IN_PROGRESS
        ticket2.expert = expert4
        ticket2.priority = 2
        ticket2.title = "Ticket sample"
        ticket2.description = "Ticket description sample"

        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)

        val history1ticket1user1expert3timestamp34 = TicketHistory()
        history1ticket1user1expert3timestamp34.ticket = ticket1
        history1ticket1user1expert3timestamp34.currentExpert = expert3
        history1ticket1user1expert3timestamp34.newState = TicketStatus.CLOSED
        history1ticket1user1expert3timestamp34.oldState = TicketStatus.IN_PROGRESS
        history1ticket1user1expert3timestamp34.updatedTimestamp = Timestamp(34)
        history1ticket1user1expert3timestamp34.user = customer1

        val history2ticket1user2expert3timestamp34 = TicketHistory()
        history2ticket1user2expert3timestamp34.ticket = ticket1
        history2ticket1user2expert3timestamp34.currentExpert = expert3
        history2ticket1user2expert3timestamp34.newState = TicketStatus.RESOLVED
        history2ticket1user2expert3timestamp34.oldState = TicketStatus.OPEN
        history2ticket1user2expert3timestamp34.updatedTimestamp = Timestamp(34)
        history2ticket1user2expert3timestamp34.user = customer2

        val history3ticket2user1expert3timestamp34 = TicketHistory()
        history3ticket2user1expert3timestamp34.ticket = ticket2
        history3ticket2user1expert3timestamp34.currentExpert = expert3
        history3ticket2user1expert3timestamp34.newState = TicketStatus.CLOSED
        history3ticket2user1expert3timestamp34.oldState = TicketStatus.IN_PROGRESS
        history3ticket2user1expert3timestamp34.updatedTimestamp = Timestamp(34)
        history3ticket2user1expert3timestamp34.user = customer1

        val history4ticket1user1expert4timestamp34 = TicketHistory()
        history4ticket1user1expert4timestamp34.ticket = ticket1
        history4ticket1user1expert4timestamp34.currentExpert = expert4
        history4ticket1user1expert4timestamp34.newState = TicketStatus.CLOSED
        history4ticket1user1expert4timestamp34.oldState = TicketStatus.IN_PROGRESS
        history4ticket1user1expert4timestamp34.updatedTimestamp = Timestamp(34)
        history4ticket1user1expert4timestamp34.user = customer1

        val history5ticket1user1expert3timestamp43 = TicketHistory()
        history5ticket1user1expert3timestamp43.ticket = ticket1
        history5ticket1user1expert3timestamp43.currentExpert = expert3
        history5ticket1user1expert3timestamp43.newState = TicketStatus.CLOSED
        history5ticket1user1expert3timestamp43.oldState = TicketStatus.IN_PROGRESS
        history5ticket1user1expert3timestamp43.updatedTimestamp = Timestamp(43)
        history5ticket1user1expert3timestamp43.user = customer1

        ticketHistoryRepository.save(history1ticket1user1expert3timestamp34)
        ticketHistoryRepository.save(history2ticket1user2expert3timestamp34)
        ticketHistoryRepository.save(history3ticket2user1expert3timestamp34)
        ticketHistoryRepository.save(history4ticket1user1expert4timestamp34)
        ticketHistoryRepository.save(history5ticket1user1expert3timestamp43)

        val url = "http://localhost:$port/API/ticketing/history/filter?ticketId=1&userEmail=${customer1.email}&currentExpertEmail=${expert3.email}&updatedAfter=${Timestamp(33).toLocalDateTime()}&updatedBefore=${Timestamp(44).toLocalDateTime()}"
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

        Assertions.assertEquals(body.size, 2)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(body[0]["historyId"], history1ticket1user1expert3timestamp34.historyId)
        Assertions.assertEquals(body[0]["ticketId"], history1ticket1user1expert3timestamp34.ticket!!.ticketId)
        Assertions.assertEquals(body[0]["newState"], history1ticket1user1expert3timestamp34.newState.name)
        Assertions.assertEquals(body[0]["oldState"], history1ticket1user1expert3timestamp34.oldState.name)
        Assertions.assertEquals(body[0]["currentExpertEmail"], history1ticket1user1expert3timestamp34.currentExpert!!.email)
        Assertions.assertEquals(body[0]["userEmail"], history1ticket1user1expert3timestamp34.user!!.email)
        Assertions.assertTrue(body[0]["updatedTimestamp"].toString().startsWith(history1ticket1user1expert3timestamp34.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        Assertions.assertEquals(body[1]["historyId"], history5ticket1user1expert3timestamp43.historyId)
        Assertions.assertEquals(body[1]["ticketId"], history5ticket1user1expert3timestamp43.ticket!!.ticketId)
        Assertions.assertEquals(body[1]["newState"], history5ticket1user1expert3timestamp43.newState.name)
        Assertions.assertEquals(body[1]["oldState"], history5ticket1user1expert3timestamp43.oldState.name)
        Assertions.assertEquals(body[1]["currentExpertEmail"], history5ticket1user1expert3timestamp43.currentExpert!!.email)
        Assertions.assertEquals(body[1]["userEmail"], history5ticket1user1expert3timestamp43.user!!.email)
        Assertions.assertTrue(body[1]["updatedTimestamp"].toString().startsWith(history5ticket1user1expert3timestamp43.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        ticketHistoryRepository.delete(history1ticket1user1expert3timestamp34)
        ticketHistoryRepository.delete(history2ticket1user2expert3timestamp34)
        ticketHistoryRepository.delete(history3ticket2user1expert3timestamp34)
        ticketHistoryRepository.delete(history4ticket1user1expert4timestamp34)
        ticketHistoryRepository.delete(history5ticket1user1expert3timestamp43)
        ticketRepository.delete(ticket1)
        ticketRepository.delete(ticket2)
        profileRepository.delete(customer1)
        profileRepository.delete(customer2)
        profileRepository.delete(expert3)
        profileRepository.delete(expert4)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }
}