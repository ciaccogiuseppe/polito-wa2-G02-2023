package it.polito.wa2.server

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
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.json.BasicJsonParser
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
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

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") {"create-drop"}
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
    fun getAllTicketHistory() {
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

        val url = "http://localhost:$port/API/ticketing/history/"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}

        Assertions.assertEquals(body.size, 3)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(body[0]["historyId"], history1ticket1.historyId)
        Assertions.assertEquals(body[0]["ticketId"], history1ticket1.ticket!!.ticketId)
        Assertions.assertEquals(body[0]["newState"], history1ticket1.newState.name)
        Assertions.assertEquals(body[0]["oldState"], history1ticket1.oldState.name)
        Assertions.assertEquals(body[0]["currentExpertId"], history1ticket1.currentExpert!!.profileId)
        Assertions.assertEquals(body[0]["userId"], history1ticket1.user!!.profileId)
        Assertions.assertTrue(body[0]["updatedTimestamp"].toString().startsWith(history1ticket1.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        Assertions.assertEquals(body[1]["historyId"], history2ticket2.historyId)
        Assertions.assertEquals(body[1]["ticketId"], history2ticket2.ticket!!.ticketId)
        Assertions.assertEquals(body[1]["newState"], history2ticket2.newState.name)
        Assertions.assertEquals(body[1]["oldState"], history2ticket2.oldState.name)
        Assertions.assertEquals(body[1]["currentExpertId"], history2ticket2.currentExpert!!.profileId)
        Assertions.assertEquals(body[1]["userId"], history2ticket2.user!!.profileId)
        Assertions.assertTrue(body[1]["updatedTimestamp"].toString().startsWith(history2ticket2.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        Assertions.assertEquals(body[2]["historyId"], history3ticket2.historyId)
        Assertions.assertEquals(body[2]["ticketId"], history3ticket2.ticket!!.ticketId)
        Assertions.assertEquals(body[2]["newState"], history3ticket2.newState.name)
        Assertions.assertEquals(body[2]["oldState"], history3ticket2.oldState.name)
        Assertions.assertEquals(body[2]["currentExpertId"], history3ticket2.currentExpert!!.profileId)
        Assertions.assertEquals(body[2]["userId"], history3ticket2.user!!.profileId)
        Assertions.assertTrue(body[2]["updatedTimestamp"].toString().startsWith(history3ticket2.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        ticketHistoryRepository.delete(history1ticket1)
        ticketHistoryRepository.delete(history2ticket2)
        ticketHistoryRepository.delete(history3ticket2)
        ticketRepository.delete(ticket1)
        ticketRepository.delete(ticket2)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun getAllTicketHistoryEmpty() {
        val url = "http://localhost:$port/API/ticketing/history/"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}

        Assertions.assertEquals(body.size, 0)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
    }

    @Test
    @DirtiesContext
    fun getExistingTicketHistory() {
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

        val url = "http://localhost:$port/API/ticketing/history/2"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}

        Assertions.assertEquals(body.size, 2)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(body[0]["historyId"], history2ticket2.historyId)
        Assertions.assertEquals(body[0]["ticketId"], history2ticket2.ticket!!.ticketId)
        Assertions.assertEquals(body[0]["newState"], history2ticket2.newState.name)
        Assertions.assertEquals(body[0]["oldState"], history2ticket2.oldState.name)
        Assertions.assertEquals(body[0]["currentExpertId"], history2ticket2.currentExpert!!.profileId)
        Assertions.assertEquals(body[0]["userId"], history2ticket2.user!!.profileId)
        Assertions.assertTrue(body[0]["updatedTimestamp"].toString().startsWith(history2ticket2.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        Assertions.assertEquals(body[1]["historyId"], history3ticket2.historyId)
        Assertions.assertEquals(body[1]["ticketId"], history3ticket2.ticket!!.ticketId)
        Assertions.assertEquals(body[1]["newState"], history3ticket2.newState.name)
        Assertions.assertEquals(body[1]["oldState"], history3ticket2.oldState.name)
        Assertions.assertEquals(body[1]["currentExpertId"], history3ticket2.currentExpert!!.profileId)
        Assertions.assertEquals(body[1]["userId"], history3ticket2.user!!.profileId)
        Assertions.assertTrue(body[1]["updatedTimestamp"].toString().startsWith(history3ticket2.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        ticketHistoryRepository.delete(history1ticket1)
        ticketHistoryRepository.delete(history2ticket2)
        ticketHistoryRepository.delete(history3ticket2)
        ticketRepository.delete(ticket1)
        ticketRepository.delete(ticket2)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun getExistingTicketHistoryEmpty() {
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

        val url = "http://localhost:$port/API/ticketing/history/2"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}

        Assertions.assertEquals(body.size, 0)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        ticketHistoryRepository.delete(history1ticket1)
        ticketRepository.delete(ticket1)
        ticketRepository.delete(ticket2)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun getNonExistingTicketHistory() {
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

        val url = "http://localhost:$port/API/ticketing/history/2"
        val uri = URI(url)

        val result = restTemplate.getForEntity(uri, String::class.java)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

        ticketHistoryRepository.delete(history1ticket1)
        ticketRepository.delete(ticket1)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryWrongIdNegative() {
        val url = "http://localhost:$port/API/ticketing/history/-3"
        val uri = URI(url)

        val result = restTemplate.getForEntity(uri, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryWrongIdAlphabetic() {
        val url = "http://localhost:$port/API/ticketing/history/a"
        val uri = URI(url)

        val result = restTemplate.getForEntity(uri, String::class.java)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }
}