package it.polito.wa2.server


import dasniko.testcontainers.keycloak.KeycloakContainer
import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileRole
import it.polito.wa2.server.ticketing.ticket.*
import it.polito.wa2.server.ticketing.tickethistory.TicketHistoryRepository
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

        /*@JvmStatic
        @AfterAll
        fun clean(){
            keycloak.stop()
            postgres.close()
        }*/


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
    //@DirtiesContext
    fun getExistingTicketManager() {
        val customer = TestUtils.testProfile("mario.rossi@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val uri = URI("http://localhost:$port/API/manager/ticketing/${ticket.getId()}")

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseMap(result.body)
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
    //@DirtiesContext
    fun getExistingTicketAuthorizedClient() {
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val uri = URI("http://localhost:$port/API/client/ticketing/${ticket.getId()}")

        val entity = TestUtils.testEntityHeader(null, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseMap(result.body)
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
    //@DirtiesContext
    fun getExistingTicketAuthorizedExpert() {
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val uri = URI("http://localhost:$port/API/expert/ticketing/${ticket.getId()}")

        val entity = TestUtils.testEntityHeader(null, expertToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseMap(result.body)
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
    //@DirtiesContext
    fun getExistingTicketForbiddenClient() {
        val customer = TestUtils.testProfile("client1@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val uri = URI("http://localhost:$port/API/client/ticketing/${ticket.getId()}")

        val entity = TestUtils.testEntityHeader(null, clientToken)

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
    //@DirtiesContext
    fun getExistingTicketForbiddenExpert() {
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert1@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val uri = URI("http://localhost:$port/API/expert/ticketing/${ticket.getId()}")

        val entity = TestUtils.testEntityHeader(null, expertToken)

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
    //@DirtiesContext
    fun getNonExistingTicket() {
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        val uri = URI("http://localhost:$port/API/manager/ticketing/1")

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun getWrongIdTicket() {
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        val uri = URI("http://localhost:$port/API/manager/ticketing/abc")

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
    //@DirtiesContext
    fun getFilteredTicketsSingleCustomerFilter(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val customer2 = TestUtils.testProfile("client2@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val expert2 = TestUtils.testProfile("luigi.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket1 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        val ticket2 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert2, 2, "Ticket sample", "Ticket description sample")
        val ticket3 = TestUtils.testTicket(Timestamp(0), product, customer2, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val uri = URI("http://localhost:$port/API/manager/ticketing/filter?customerEmail=${customer.email}")

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
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
    //@DirtiesContext
    fun getFilteredTicketsSingleCustomerFilterAuthorizedClient(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val customer2 = TestUtils.testProfile("client2@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val expert2 = TestUtils.testProfile("luigi.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket1 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        val ticket2 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert2, 2, "Ticket sample", "Ticket description sample")
        val ticket3 = TestUtils.testTicket(Timestamp(0), product, customer2, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val uri = URI("http://localhost:$port/API/client/ticketing/filter?customerEmail=${customer.email}")

        val entity = TestUtils.testEntityHeader(null, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )


        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
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
    //@DirtiesContext
    fun getFilteredTicketsSingleCustomerFilterOtherClient(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val customer2 = TestUtils.testProfile("client2@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val expert2 = TestUtils.testProfile("luigi.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket1 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        val ticket2 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert2, 2, "Ticket sample", "Ticket description sample")
        val ticket3 = TestUtils.testTicket(Timestamp(0), product, customer2, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val uri = URI("http://localhost:$port/API/client/ticketing/filter?customerEmail=${customer2.email}")

        val entity = TestUtils.testEntityHeader(null, clientToken)

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
    //@DirtiesContext
    fun getFilteredTicketsMultipleUserFilter(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val customer2 = TestUtils.testProfile("client2@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val expert2 = TestUtils.testProfile("luigi.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket1 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        val ticket2 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert2, 2, "Ticket sample", "Ticket description sample")
        val ticket3 = TestUtils.testTicket(Timestamp(0), product, customer2, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val uri = URI("http://localhost:$port/API/manager/ticketing/filter?customerEmail=${customer.email}&expertEmail=${expert2.email}")

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
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
    //@DirtiesContext
    fun getFilteredTicketsMultipleUserFilterOneClientOnly(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val customer2 = TestUtils.testProfile("client2@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val expert2 = TestUtils.testProfile("luigi.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket1 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        val ticket2 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert2, 2, "Ticket sample", "Ticket description sample")
        val ticket3 = TestUtils.testTicket(Timestamp(0), product, customer2, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val uri = URI("http://localhost:$port/API/client/ticketing/filter?customerEmail=${customer.email}&expertEmail=${expert2.email}")

        val entity = TestUtils.testEntityHeader(null, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
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
    //@DirtiesContext
    fun getFilteredTicketsTimeRange(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val customer2 = TestUtils.testProfile("client2@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val expert2 = TestUtils.testProfile("luigi.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket1 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        val ticket2 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert2, 2, "Ticket sample", "Ticket description sample")
        val ticket3 = TestUtils.testTicket(Timestamp(3), product, customer2, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val uri = URI("http://localhost:$port/API/manager/ticketing/filter?createdAfter=${Timestamp(0).toLocalDateTime()}&createdBefore=${Timestamp(1).toLocalDateTime()}")

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
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
    //@DirtiesContext
    fun getFilteredTicketsPriorityRange(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val customer2 = TestUtils.testProfile("client2@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val expert2 = TestUtils.testProfile("luigi.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket1 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 1, "Ticket sample", "Ticket description sample")
        val ticket2 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert2, 2, "Ticket sample", "Ticket description sample")
        val ticket3 = TestUtils.testTicket(Timestamp(0), product, customer2, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val uri = URI("http://localhost:$port/API/manager/ticketing/filter?minPriority=2&maxPriority=3")

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
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
    //@DirtiesContext
    fun getFilteredTicketsProduct(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val customer2 = TestUtils.testProfile("client2@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val expert2 = TestUtils.testProfile("luigi.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        val product2 = TestUtils.testProduct("0000000000001", "PC Omen Intel i5", "HP")
        productRepository.save(product)
        productRepository.save(product2)

        val ticket1 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        val ticket2 = TestUtils.testTicket(Timestamp(0), product2, customer, TicketStatus.IN_PROGRESS, expert2, 2, "Ticket sample", "Ticket description sample")
        val ticket3 = TestUtils.testTicket(Timestamp(0), product2, customer2, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val uri = URI("http://localhost:$port/API/manager/ticketing/filter?productId=0000000000001")

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
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
    //@DirtiesContext
    fun getFilteredTicketsStatus(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val customer2 = TestUtils.testProfile("client2@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val expert2 = TestUtils.testProfile("luigi.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        val product2 = TestUtils.testProduct("0000000000001", "PC Omen Intel i5", "HP")
        productRepository.save(product)
        productRepository.save(product2)

        val ticket1 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.OPEN, expert, 2, "Ticket sample", "Ticket description sample")
        val ticket2 = TestUtils.testTicket(Timestamp(0), product2, customer, TicketStatus.IN_PROGRESS, expert2, 2, "Ticket sample", "Ticket description sample")
        val ticket3 = TestUtils.testTicket(Timestamp(0), product2, customer2, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val uri = URI("http://localhost:$port/API/manager/ticketing/filter?status=IN_PROGRESS")

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
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
    //@DirtiesContext
    fun getFilteredTicketsAllFilters(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val customer2 = TestUtils.testProfile("client2@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val expert2 = TestUtils.testProfile("luigi.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)
        profileRepository.save(expert2)

        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        val product2 = TestUtils.testProduct("0000000000001", "PC Omen Intel i5", "HP")
        productRepository.save(product)
        productRepository.save(product2)

        val ticket1 = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.OPEN, expert, 2, "Ticket sample", "Ticket description sample")
        val ticket2 = TestUtils.testTicket(Timestamp(0), product2, customer, TicketStatus.IN_PROGRESS, expert2, 2, "Ticket sample", "Ticket description sample")
        val ticket3 = TestUtils.testTicket(Timestamp(0), product2, customer2, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket1)
        ticketRepository.save(ticket2)
        ticketRepository.save(ticket3)

        val uri = URI("http://localhost:$port/API/manager/ticketing/filter?customerEmail=${customer.email}&expertEmail=${expert.email}&status=OPEN&productId=0000000000000&minPriority=1&maxPriority=2&createdAfter=${Timestamp(0).toLocalDateTime()}&createdBefore=${Timestamp(1).toLocalDateTime()}")

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
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
    //@DirtiesContext
    fun addTicketSuccessfulClient(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
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

        val uri = URI("http://localhost:$port/API/client/ticketing/")

        val entity = TestUtils.testEntityHeader(ticket, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
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
    //@DirtiesContext
    fun addTicketByExpertError(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
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

        val uri = URI("http://localhost:$port/API/client/ticketing/")

        val entity = TestUtils.testEntityHeader(ticket, expertToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun addTicketByManagerError(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
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

        val uri = URI("http://localhost:$port/API/client/ticketing/")

        val entity = TestUtils.testEntityHeader(ticket, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }


    @Test
    //@DirtiesContext
    fun addTicketProductNotFound(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
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

        val uri = URI("http://localhost:$port/API/client/ticketing/")

        val entity = TestUtils.testEntityHeader(ticket, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun addTicketProductWrongFormat(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
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

        val uri = URI("http://localhost:$port/API/client/ticketing/")

        val entity = TestUtils.testEntityHeader(ticket, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun addTicketWithStatus(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
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

        val uri = URI("http://localhost:$port/API/client/ticketing/")

        val entity = TestUtils.testEntityHeader(ticket, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            entity,
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
    //@DirtiesContext
    fun assignTicketSuccessfulManager(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.OPEN, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketAssign = TicketAssignDTO(
            ticket.getId()!!,
            expert.email,
            2
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/assign")

        val entity = TestUtils.testEntityHeader(ticketAssign, managerToken)

        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun assignTicketForbiddenClient(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.OPEN, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketAssign = TicketAssignDTO(
            ticket.getId()!!,
            expert.email,
            2
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/assign")

        val entity = TestUtils.testEntityHeader(ticketAssign, clientToken)

        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun assignTicketForbiddenExpert(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.OPEN, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketAssign = TicketAssignDTO(
            ticket.getId()!!,
            expert.email,
            2
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/assign")

        val entity = TestUtils.testEntityHeader(ticketAssign, expertToken)

        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun assignReopenedTicketSuccessfulManager(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.REOPENED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketAssign = TicketAssignDTO(
            ticket.getId()!!,
            expert.email,
            2
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/assign")


        val entity = TestUtils.testEntityHeader(ticketAssign, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun assignTicketToCustomer(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.REOPENED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketAssign = TicketAssignDTO(
            ticket.getId()!!,
            customer.email,
            2
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/assign")

        val entity = TestUtils.testEntityHeader(ticketAssign, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun wrongReassignTicket(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.OPEN, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketAssign = TicketAssignDTO(
            ticket.getId()!!,
            expert.email,
            2
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/assign")

        val entity = TestUtils.testEntityHeader(ticketAssign, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)
        val result2 = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

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
    //@DirtiesContext
    fun assignToClosedTicket(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.CLOSED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketAssign = TicketAssignDTO(
            ticket.getId()!!,
            expert.email,
            2
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/assign")

        val entity = TestUtils.testEntityHeader(ticketAssign, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun updateTicketSuccessfulInProgressToClosedClient(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.CLOSED
        )

        val uri = URI("http://localhost:$port/API/client/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, clientToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun updateTicketSuccessfulInProgressToClosedManager(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.CLOSED
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun updateTicketSuccessfulInProgressToOpenManager(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.OPEN
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun updateTicketForbiddenInProgressToOpenClient(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.OPEN
        )

        val uri = URI("http://localhost:$port/API/client/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, clientToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun updateTicketForbiddenInProgressToOpenExpert(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.OPEN
        )

        val uri = URI("http://localhost:$port/API/expert/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, expertToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun updateTicketSuccessfulOpenToClosedClient(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.OPEN, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.CLOSED
        )

        val uri = URI("http://localhost:$port/API/client/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, clientToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun updateTicketSuccessfulOpenToClosedManager(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.OPEN, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.CLOSED
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun updateTicketSuccessfulOpenToClosedExpert(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.OPEN, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.CLOSED
        )

        val uri = URI("http://localhost:$port/API/expert/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, expertToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun updateTicketSuccessfulResolvedToClosedClient(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.RESOLVED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.CLOSED
        )

        val uri = URI("http://localhost:$port/API/client/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, clientToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun updateTicketSuccessfulResolvedToClosedManager(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.RESOLVED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.CLOSED
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun updateTicketSuccessfulResolvedToClosedExpert(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.RESOLVED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.CLOSED
        )

        val uri = URI("http://localhost:$port/API/expert/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, expertToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun updateTicketForbiddenResolvedToClosedOtherClient(){
        val customer = TestUtils.testProfile("mario.rossi@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val customer2 = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(customer2)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.RESOLVED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.CLOSED
        )

        val uri = URI("http://localhost:$port/API/client/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, clientToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun updateTicketForbiddenResolvedToClosedOtherExpert(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val expert2 = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)
        profileRepository.save(expert2)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.RESOLVED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.CLOSED
        )

        val uri = URI("http://localhost:$port/API/expert/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, expertToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        profileRepository.delete(expert2)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun updateTicketSuccessfulReopenedToClosedClient(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.REOPENED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.CLOSED
        )

        val uri = URI("http://localhost:$port/API/client/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, clientToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun updateTicketSuccessfulReopenedToClosedExpert(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.REOPENED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.CLOSED
        )

        val uri = URI("http://localhost:$port/API/expert/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, expertToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun updateTicketSuccessfulReopenedToClosedManager(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.REOPENED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.CLOSED
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun updateTicketSuccessfulOpenToResolvedClient(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.OPEN, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.RESOLVED
        )

        val uri = URI("http://localhost:$port/API/client/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, clientToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun updateTicketSuccessfulOpenToResolvedManager(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.OPEN, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.RESOLVED
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun updateTicketSuccessfulOpenToResolvedExpert(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.OPEN, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.RESOLVED
        )

        val uri = URI("http://localhost:$port/API/expert/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, expertToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun updateTicketSuccessfulInProgressToResolved(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.RESOLVED
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun updateTicketSuccessfulReopenedToResolved(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.REOPENED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.RESOLVED
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun updateTicketWrongReopenedToInProgress(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.REOPENED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.IN_PROGRESS
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun updateTicketSuccessfulClosedToReopen(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.CLOSED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.REOPENED
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun updateTicketSuccessfulResolvedToReopen(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.RESOLVED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.REOPENED
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        val updatedTicket = ticketRepository.findById(ticket.getId()!!)

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
    //@DirtiesContext
    fun updateTicketWrongOpenToReopened(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.OPEN, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.REOPENED
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun updateTicketWrongClosedToOpen(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.CLOSED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.OPEN
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun updateTicketWrongClosedToInProgress(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.CLOSED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.IN_PROGRESS
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun updateTicketWrongClosedToResolved(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.CLOSED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.RESOLVED
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun updateTicketWrongInProgressToReopened(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.REOPENED
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun updateTicketWrongReopenedToOpen(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.REOPENED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.OPEN
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun updateTicketWrongResolvedToInProgress(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.RESOLVED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.IN_PROGRESS
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    @Test
    //@DirtiesContext
    fun updateTicketWrongResolvedToOpen(){
        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", "HP")
        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.RESOLVED, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val ticketUpdate = TicketUpdateDTO(
            ticket.getId()!!,
            TicketStatus.OPEN
        )

        val uri = URI("http://localhost:$port/API/manager/ticketing/update")

        val entity = TestUtils.testEntityHeader(ticketUpdate, managerToken)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, entity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }
}

