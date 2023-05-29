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
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter")

        val entity = TestUtils.testEntityHeader(null, managerToken)
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
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter")

        val entity = TestUtils.testEntityHeader(null, clientToken)

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
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter")

        val entity = TestUtils.testEntityHeader(null, expertToken)

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
        val customer = TestUtils.testProfile("mario.rossi@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket1 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        val ticket2 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)

        val history1ticket1 = TestUtils.testTicketHistory(ticket1, expert, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS, Timestamp(34), customer)
        ticketHistoryRepository.save(history1ticket1)

        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter?ticketId=2")

        val entity = TestUtils.testEntityHeader(null, managerToken)

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
        val customer = TestUtils.testProfile("mario.rossi@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket1 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        val ticket2 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)

        val history1ticket1 = TestUtils.testTicketHistory(ticket1, expert, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS, Timestamp(34), customer)
        val history2ticket2 = TestUtils.testTicketHistory(ticket2, expert, TicketStatus.RESOLVED, TicketStatus.OPEN, Timestamp(19), customer)
        val history3ticket2 = TestUtils.testTicketHistory(ticket2, expert, TicketStatus.REOPENED, TicketStatus.RESOLVED, Timestamp(122), customer)
        ticketHistoryRepository.save(history1ticket1)
        ticketHistoryRepository.save(history2ticket2)
        ticketHistoryRepository.save(history3ticket2)


        val uri= URI("http://localhost:$port/API/manager/ticketing/history/filter?ticketId=2")

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}

        Assertions.assertEquals(body.size, 2)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(body[0]["historyId"], history2ticket2.getId())
        Assertions.assertEquals(body[0]["ticketId"], history2ticket2.ticket!!.getId())
        Assertions.assertEquals(body[0]["newState"], history2ticket2.newState.name)
        Assertions.assertEquals(body[0]["oldState"], history2ticket2.oldState.name)
        Assertions.assertEquals(body[0]["currentExpertEmail"], history2ticket2.currentExpert!!.email)
        Assertions.assertEquals(body[0]["userEmail"], history2ticket2.user!!.email)
        Assertions.assertTrue(body[0]["updatedTimestamp"].toString().startsWith(history2ticket2.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        Assertions.assertEquals(body[1]["historyId"], history3ticket2.getId())
        Assertions.assertEquals(body[1]["ticketId"], history3ticket2.ticket!!.getId())
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
        val customer = TestUtils.testProfile("mario.rossi@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket1 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket1)

        val history1ticket1 = TestUtils.testTicketHistory(ticket1, expert, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS, Timestamp(34), customer)
        ticketHistoryRepository.save(history1ticket1)


        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter?ticketId=2")

        val entity = TestUtils.testEntityHeader(null, managerToken)

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
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter?ticketId=-3")

        val entity = TestUtils.testEntityHeader(null, managerToken)

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
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter?ticketId=a")

        val entity = TestUtils.testEntityHeader(null, managerToken)

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
        val customer1 = TestUtils.testProfile("mario.rossi@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val customer2 = TestUtils.testProfile("luigi.verdi@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer1)
        profileRepository.save(customer2)
        profileRepository.save(expert)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer1, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val history1customer1 = TestUtils.testTicketHistory(ticket, expert, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS, Timestamp(34), customer1)
        val history2customer2 = TestUtils.testTicketHistory(ticket, expert, TicketStatus.RESOLVED, TicketStatus.OPEN, Timestamp(19), customer2)
        val history3customer2 = TestUtils.testTicketHistory(ticket, expert, TicketStatus.REOPENED, TicketStatus.RESOLVED, Timestamp(122), customer2)
        ticketHistoryRepository.save(history1customer1)
        ticketHistoryRepository.save(history2customer2)
        ticketHistoryRepository.save(history3customer2)

        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter?userEmail=${customer2.email}")

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}

        Assertions.assertEquals(body.size, 2)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(body[0]["historyId"], history2customer2.getId())
        Assertions.assertEquals(body[0]["ticketId"], history2customer2.ticket!!.getId())
        Assertions.assertEquals(body[0]["newState"], history2customer2.newState.name)
        Assertions.assertEquals(body[0]["oldState"], history2customer2.oldState.name)
        Assertions.assertEquals(body[0]["currentExpertEmail"], history2customer2.currentExpert!!.email)
        Assertions.assertEquals(body[0]["userEmail"], history2customer2.user!!.email)
        Assertions.assertTrue(body[0]["updatedTimestamp"].toString().startsWith(history2customer2.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        Assertions.assertEquals(body[1]["historyId"], history3customer2.getId())
        Assertions.assertEquals(body[1]["ticketId"], history3customer2.ticket!!.getId())
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
        val customer1 = TestUtils.testProfile("mario.rossi@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer1)
        profileRepository.save(expert)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer1, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val history1customer1 = TestUtils.testTicketHistory(ticket, expert, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS, Timestamp(34), customer1)
        ticketHistoryRepository.save(history1customer1)

        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter?userEmail=not.found@polito.it")

        val entity = TestUtils.testEntityHeader(null, managerToken)

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
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter?userEmail=invalidEmail")

        val entity = TestUtils.testEntityHeader(null, managerToken)

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
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter?userEmail=33")

        val entity = TestUtils.testEntityHeader(null, managerToken)

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
        val customer1 = TestUtils.testProfile("mario.rossi@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert2 = TestUtils.testProfile("luigi.verdi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val expert3 = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer1)
        profileRepository.save(expert2)
        profileRepository.save(expert3)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer1, TicketStatus.IN_PROGRESS, expert3, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val history1expert2 = TestUtils.testTicketHistory(ticket, expert2, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS, Timestamp(34), customer1)
        val history2expert3 = TestUtils.testTicketHistory(ticket, expert3, TicketStatus.RESOLVED, TicketStatus.OPEN, Timestamp(19), customer1)
        val history3expert3 = TestUtils.testTicketHistory(ticket, expert3, TicketStatus.REOPENED, TicketStatus.RESOLVED, Timestamp(122), customer1)
        ticketHistoryRepository.save(history1expert2)
        ticketHistoryRepository.save(history2expert3)
        ticketHistoryRepository.save(history3expert3)

        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter?currentExpertEmail=${expert3.email}")

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}

        Assertions.assertEquals(body.size, 2)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(body[0]["historyId"], history2expert3.getId())
        Assertions.assertEquals(body[0]["ticketId"], history2expert3.ticket!!.getId())
        Assertions.assertEquals(body[0]["newState"], history2expert3.newState.name)
        Assertions.assertEquals(body[0]["oldState"], history2expert3.oldState.name)
        Assertions.assertEquals(body[0]["currentExpertEmail"], history2expert3.currentExpert!!.email)
        Assertions.assertEquals(body[0]["userEmail"], history2expert3.user!!.email)
        Assertions.assertTrue(body[0]["updatedTimestamp"].toString().startsWith(history2expert3.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        Assertions.assertEquals(body[1]["historyId"], history3expert3.getId())
        Assertions.assertEquals(body[1]["ticketId"], history3expert3.ticket!!.getId())
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
        val customer1 = TestUtils.testProfile("mario.rossi@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer1)
        profileRepository.save(expert)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer1, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val history1customer1 = TestUtils.testTicketHistory(ticket, expert, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS, Timestamp(34), customer1)
        ticketHistoryRepository.save(history1customer1)

        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter?currentExpertEmail=not.found@polito.it")

        val entity = TestUtils.testEntityHeader(null, managerToken)

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
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter?currentExpertEmail=invalidEmail")

        val entity = TestUtils.testEntityHeader(null, managerToken)

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
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter?currentExpertEmail=33")

        val entity = TestUtils.testEntityHeader(null, managerToken)

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
        val customer1 = TestUtils.testProfile("mario.rossi@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert2 = TestUtils.testProfile("luigi.verdi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer1)
        profileRepository.save(expert2)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer1, TicketStatus.IN_PROGRESS, expert2, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val history1timestamp10 = TestUtils.testTicketHistory(ticket, expert2, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS, Timestamp(10), customer1)
        val history2timestamp20 = TestUtils.testTicketHistory(ticket, expert2, TicketStatus.RESOLVED, TicketStatus.OPEN, Timestamp(20), customer1)
        val history3timestamp30 = TestUtils.testTicketHistory(ticket, expert2, TicketStatus.REOPENED, TicketStatus.RESOLVED, Timestamp(30), customer1)
        ticketHistoryRepository.save(history1timestamp10)
        ticketHistoryRepository.save(history2timestamp20)
        ticketHistoryRepository.save(history3timestamp30)

        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter?updatedAfter=${Timestamp(20).toLocalDateTime()}")

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}

        Assertions.assertEquals(body.size, 2)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(body[0]["historyId"], history2timestamp20.getId())
        Assertions.assertEquals(body[0]["ticketId"], history2timestamp20.ticket!!.getId())
        Assertions.assertEquals(body[0]["newState"], history2timestamp20.newState.name)
        Assertions.assertEquals(body[0]["oldState"], history2timestamp20.oldState.name)
        Assertions.assertEquals(body[0]["currentExpertEmail"], history2timestamp20.currentExpert!!.email)
        Assertions.assertEquals(body[0]["userEmail"], history2timestamp20.user!!.email)
        Assertions.assertTrue(body[0]["updatedTimestamp"].toString().startsWith(history2timestamp20.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        Assertions.assertEquals(body[1]["historyId"], history3timestamp30.getId())
        Assertions.assertEquals(body[1]["ticketId"], history3timestamp30.ticket!!.getId())
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
        val customer1 = TestUtils.testProfile("mario.rossi@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert2 = TestUtils.testProfile("luigi.verdi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer1)
        profileRepository.save(expert2)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer1, TicketStatus.IN_PROGRESS, expert2, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val history1timestamp10 = TestUtils.testTicketHistory(ticket, expert2, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS, Timestamp(10), customer1)
        val history2timestamp20 = TestUtils.testTicketHistory(ticket, expert2, TicketStatus.RESOLVED, TicketStatus.OPEN, Timestamp(20), customer1)
        val history3timestamp30 = TestUtils.testTicketHistory(ticket, expert2, TicketStatus.REOPENED, TicketStatus.RESOLVED, Timestamp(30), customer1)
        ticketHistoryRepository.save(history1timestamp10)
        ticketHistoryRepository.save(history2timestamp20)
        ticketHistoryRepository.save(history3timestamp30)

        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter?updatedBefore=${Timestamp(19).toLocalDateTime()}")

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}

        Assertions.assertEquals(body.size, 1)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(body[0]["historyId"], history1timestamp10.getId())
        Assertions.assertEquals(body[0]["ticketId"], history1timestamp10.ticket!!.getId())
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
        val customer1 = TestUtils.testProfile("mario.rossi@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert2 = TestUtils.testProfile("luigi.verdi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer1)
        profileRepository.save(expert2)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer1, TicketStatus.IN_PROGRESS, expert2, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val history1timestamp10 = TestUtils.testTicketHistory(ticket, expert2, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS, Timestamp(10), customer1)
        val history2timestamp20 = TestUtils.testTicketHistory(ticket, expert2, TicketStatus.RESOLVED, TicketStatus.OPEN, Timestamp(20), customer1)
        val history3timestamp30 = TestUtils.testTicketHistory(ticket, expert2, TicketStatus.REOPENED, TicketStatus.RESOLVED, Timestamp(30), customer1)
        ticketHistoryRepository.save(history1timestamp10)
        ticketHistoryRepository.save(history2timestamp20)
        ticketHistoryRepository.save(history3timestamp30)

        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter?updatedAfter=${Timestamp(9).toLocalDateTime()}&updatedBefore=${Timestamp(10).toLocalDateTime()}")

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}

        Assertions.assertEquals(body.size, 1)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(body[0]["historyId"], history1timestamp10.getId())
        Assertions.assertEquals(body[0]["ticketId"], history1timestamp10.ticket!!.getId())
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
        val customer1 = TestUtils.testProfile("mario.rossi@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert2 = TestUtils.testProfile("luigi.verdi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer1)
        profileRepository.save(expert2)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer1, TicketStatus.IN_PROGRESS, expert2, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val history1timestamp10 = TestUtils.testTicketHistory(ticket, expert2, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS, Timestamp(10), customer1)
        val history2timestamp20 = TestUtils.testTicketHistory(ticket, expert2, TicketStatus.RESOLVED, TicketStatus.OPEN, Timestamp(20), customer1)
        val history3timestamp30 = TestUtils.testTicketHistory(ticket, expert2, TicketStatus.REOPENED, TicketStatus.RESOLVED, Timestamp(30), customer1)
        ticketHistoryRepository.save(history1timestamp10)
        ticketHistoryRepository.save(history2timestamp20)
        ticketHistoryRepository.save(history3timestamp30)

        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter?updatedAfter=${Timestamp(11).toLocalDateTime()}&updatedBefore=${Timestamp(19).toLocalDateTime()}")


        val entity = TestUtils.testEntityHeader(null, managerToken)

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
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter?updatedAfter=${Timestamp(11).toLocalDateTime()}&updatedBefore=${Timestamp(9).toLocalDateTime()}")

        val entity = TestUtils.testEntityHeader(null, managerToken)

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
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter?updatedAfter=${Timestamp(10).toLocalDateTime()}&updatedBefore=${Timestamp(10).toLocalDateTime()}")

        val entity = TestUtils.testEntityHeader(null, managerToken)

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
        val customer1 = TestUtils.testProfile("mario.rossi@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val customer2 = TestUtils.testProfile("luigi.verdi@polito.it", "Luigi", "Verdi", ProfileRole.CLIENT)
        val expert3 = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val expert4 = TestUtils.testProfile("luigi.viola@polito.it", "Luigi", "Viola", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer1)
        profileRepository.save(customer2)
        profileRepository.save(expert3)
        profileRepository.save(expert4)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket1 = TestUtils.testTicket(Timestamp(0), product, customer1, TicketStatus.IN_PROGRESS, expert3, 2, "Ticket sample", "Ticket description sample")
        val ticket2 = TestUtils.testTicket(Timestamp(0), product, customer2, TicketStatus.IN_PROGRESS, expert4, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)

        val history1ticket1user1expert3timestamp34 = TestUtils.testTicketHistory(ticket1, expert3, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS, Timestamp(34), customer1)
        val history2ticket1user2expert3timestamp34 = TestUtils.testTicketHistory(ticket1, expert3, TicketStatus.RESOLVED, TicketStatus.OPEN, Timestamp(34), customer2)
        val history3ticket2user1expert3timestamp34 = TestUtils.testTicketHistory(ticket2, expert3, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS, Timestamp(34), customer1)
        val history4ticket1user1expert4timestamp34 = TestUtils.testTicketHistory(ticket1, expert4, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS, Timestamp(34), customer1)
        val history5ticket1user1expert3timestamp43 = TestUtils.testTicketHistory(ticket1, expert3, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS, Timestamp(43), customer1)
        ticketHistoryRepository.save(history1ticket1user1expert3timestamp34)
        ticketHistoryRepository.save(history2ticket1user2expert3timestamp34)
        ticketHistoryRepository.save(history3ticket2user1expert3timestamp34)
        ticketHistoryRepository.save(history4ticket1user1expert4timestamp34)
        ticketHistoryRepository.save(history5ticket1user1expert3timestamp43)


        val uri = URI("http://localhost:$port/API/manager/ticketing/history/filter?ticketId=1&userEmail=${customer1.email}&currentExpertEmail=${expert3.email}&updatedAfter=${Timestamp(33).toLocalDateTime()}&updatedBefore=${Timestamp(44).toLocalDateTime()}")

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}

        Assertions.assertEquals(body.size, 2)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(body[0]["historyId"], history1ticket1user1expert3timestamp34.getId())
        Assertions.assertEquals(body[0]["ticketId"], history1ticket1user1expert3timestamp34.ticket!!.getId())
        Assertions.assertEquals(body[0]["newState"], history1ticket1user1expert3timestamp34.newState.name)
        Assertions.assertEquals(body[0]["oldState"], history1ticket1user1expert3timestamp34.oldState.name)
        Assertions.assertEquals(body[0]["currentExpertEmail"], history1ticket1user1expert3timestamp34.currentExpert!!.email)
        Assertions.assertEquals(body[0]["userEmail"], history1ticket1user1expert3timestamp34.user!!.email)
        Assertions.assertTrue(body[0]["updatedTimestamp"].toString().startsWith(history1ticket1user1expert3timestamp34.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        Assertions.assertEquals(body[1]["historyId"], history5ticket1user1expert3timestamp43.getId())
        Assertions.assertEquals(body[1]["ticketId"], history5ticket1user1expert3timestamp43.ticket!!.getId())
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