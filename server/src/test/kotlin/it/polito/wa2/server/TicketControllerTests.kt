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

    @Test
    @DirtiesContext
    fun getFilteredTicketsSingleCustomerFilter(){
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

        val url = "http://localhost:$port/API/ticketing/filter?customerId=${customer.profileId}"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        println(result.body)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(2, body.size)

        ticketRepository.delete(ticket3)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket1)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
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

        val url = "http://localhost:$port/API/ticketing/filter?customerId=${customer.profileId}&expertId=${expert2.profileId}"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        println(result.body)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(1, body.size)

        ticketRepository.delete(ticket3)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket1)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
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

        val url = "http://localhost:$port/API/ticketing/filter?createdAfter=${Timestamp(0).toLocalDateTime()}&createdBefore=${Timestamp(1).toLocalDateTime()}"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        println(result.body)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(2, body.size)

        ticketRepository.delete(ticket3)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket1)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
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

        val url = "http://localhost:$port/API/ticketing/filter?minPriority=2&maxPriority=3"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        println(result.body)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(2, body.size)

        ticketRepository.delete(ticket3)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket1)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
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

        val url = "http://localhost:$port/API/ticketing/filter?productId=0000000000001"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        println(result.body)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(2, body.size)

        ticketRepository.delete(ticket3)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket1)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        productRepository.delete(product2)
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

        val url = "http://localhost:$port/API/ticketing/filter?status=IN_PROGRESS"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        println(result.body)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(2, body.size)

        ticketRepository.delete(ticket3)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket1)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        productRepository.delete(product2)
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

        val url = "http://localhost:$port/API/ticketing/filter?customerId=${customer.profileId}&expertId=${expert.profileId}&status=OPEN&productId=0000000000000&minPriority=1&maxPriority=2&createdAfter=${Timestamp(0).toLocalDateTime()}&createdBefore=${Timestamp(1).toLocalDateTime()}"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        println(result.body)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(1, body.size)

        ticketRepository.delete(ticket3)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket1)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        productRepository.delete(product2)
    }

}

