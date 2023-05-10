package it.polito.wa2.server


import it.polito.wa2.server.products.Product
import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileRole
import it.polito.wa2.server.ticketing.ticket.Ticket
import it.polito.wa2.server.ticketing.ticket.TicketRepository
import it.polito.wa2.server.ticketing.ticket.TicketStatus
import jakarta.validation.constraints.NotBlank
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.json.BasicJsonParser
import org.springframework.boot.json.GsonJsonParser
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.shaded.com.google.common.reflect.TypeToken
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
    @Test
    @DirtiesContext
    fun getExistingTicket() {
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

        val url = "http://localhost:$port/API/ticketing/${ticket.ticketId}"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        val body = json.parseMap(result.body)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        Assertions.assertEquals(product.productId, body["productId"])
        Assertions.assertEquals(customer.email, body["customerId"])
        Assertions.assertEquals(expert.email, body["expertId"])
        Assertions.assertNotNull(body["status"])
        Assertions.assertEquals(ticket.status, TicketStatus.valueOf(body["status"]!!.toString()))
        Assertions.assertEquals(ticket.priority.toLong(), body["priority"])
        Assertions.assertEquals(ticket.description, body["description"])
        Assertions.assertEquals(ticket.title, body["title"])

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun getNonExistingTicket() {
        val url = "http://localhost:$port/API/ticketing/1"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        val body = json.parseMap(result.body)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
    }

    @Test
    @DirtiesContext
    fun getWrongIdTicket() {
        val url = "http://localhost:$port/API/ticketing/abc"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        val body = json.parseMap(result.body)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }
}

