package it.polito.wa2.server


import it.polito.wa2.server.products.Product
import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileRole
import it.polito.wa2.server.ticketing.ticket.*
import it.polito.wa2.server.ticketing.tickethistory.TicketHistoryRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.json.BasicJsonParser
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
    @Autowired
    lateinit var ticketHistoryRepository: TicketHistoryRepository
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
        Assertions.assertEquals(customer.profileId, body["customerId"])
        Assertions.assertEquals(expert.profileId, body["expertId"])
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
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        profileRepository.delete(expert2)
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
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        profileRepository.delete(expert2)
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
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        profileRepository.delete(expert2)
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
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        profileRepository.delete(expert2)
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
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        profileRepository.delete(expert2)
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
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        profileRepository.delete(expert2)
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
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        profileRepository.delete(expert2)
        productRepository.delete(product)
        productRepository.delete(product2)
    }


    @Test
    @DirtiesContext
    fun addTicketSuccessful(){
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
            customer.email,
            expert.email,
            null,
            null
        )

        val url = "http://localhost:$port/API/ticketing/"
        val uri = URI(url)
        val json = BasicJsonParser()

        val requestEntity : HttpEntity<TicketDTO> = HttpEntity(ticket)
        val result = restTemplate.postForEntity(uri, requestEntity, String::class.java)

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
    }

    @Test
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
    }

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

        val url = "http://localhost:$port/API/ticketing/"
        val uri = URI(url)
        val json = BasicJsonParser()

        val requestEntity : HttpEntity<TicketDTO> = HttpEntity(ticket)
        val result = restTemplate.postForEntity(uri, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
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

        val url = "http://localhost:$port/API/ticketing/"
        val uri = URI(url)
        val json = BasicJsonParser()

        val requestEntity : HttpEntity<TicketDTO> = HttpEntity(ticket)
        val result = restTemplate.postForEntity(uri, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun addTicketWithStatus(){
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
            customer.email,
            null,
            TicketStatus.IN_PROGRESS,
            null
        )

        val url = "http://localhost:$port/API/ticketing/"
        val uri = URI(url)
        val json = BasicJsonParser()

        val requestEntity : HttpEntity<TicketDTO> = HttpEntity(ticket)
        val result = restTemplate.postForEntity(uri, requestEntity, String::class.java)

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
    }

    @Test
    @DirtiesContext
    fun assignTicketSuccessful(){
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

        val url = "http://localhost:$port/API/ticketing/assign"
        val uri = URI(url)
        val json = BasicJsonParser()

        val requestEntity : HttpEntity<TicketAssignDTO> = HttpEntity(ticketAssign)
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
    }

    @Test
    @DirtiesContext
    fun assignReopenedTicketSuccessful(){
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

        val url = "http://localhost:$port/API/ticketing/assign"
        val uri = URI(url)
        val json = BasicJsonParser()

        val requestEntity : HttpEntity<TicketAssignDTO> = HttpEntity(ticketAssign)
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

        val url = "http://localhost:$port/API/ticketing/assign"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketAssignDTO> = HttpEntity(ticketAssign)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
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

        val url = "http://localhost:$port/API/ticketing/assign"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketAssignDTO> = HttpEntity(ticketAssign)
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

        val url = "http://localhost:$port/API/ticketing/assign"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketAssignDTO> = HttpEntity(ticketAssign)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulInProgressToClosed(){
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

        val url = "http://localhost:$port/API/ticketing/update"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate)
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
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulInProgressToOpen(){
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

        val url = "http://localhost:$port/API/ticketing/update"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate)
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
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulOpenToClosed(){
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

        val url = "http://localhost:$port/API/ticketing/update"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate)
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
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulResolvedToClosed(){
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

        val url = "http://localhost:$port/API/ticketing/update"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate)
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
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulReopenedToClosed(){
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

        val url = "http://localhost:$port/API/ticketing/update"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate)
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
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulOpenToResolved(){
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

        val url = "http://localhost:$port/API/ticketing/update"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate)
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
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulInProgressToResolved(){
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

        val url = "http://localhost:$port/API/ticketing/update"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate)
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
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulReopenedToResolved(){
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

        val url = "http://localhost:$port/API/ticketing/update"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate)
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
    }

    @Test
    @DirtiesContext
    fun updateTicketWrongReopenedToInProgress(){
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

        val url = "http://localhost:$port/API/ticketing/update"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulClosedToReopen(){
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

        val url = "http://localhost:$port/API/ticketing/update"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate)
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
    }

    @Test
    @DirtiesContext
    fun updateTicketSuccessfulResolvedToReopen(){
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

        val url = "http://localhost:$port/API/ticketing/update"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate)
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
    }

    @Test
    @DirtiesContext
    fun updateTicketWrongOpenToReopened(){
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

        val url = "http://localhost:$port/API/ticketing/update"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun updateTicketWrongClosedToOpen(){
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

        val url = "http://localhost:$port/API/ticketing/update"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun updateTicketWrongClosedToInProgress(){
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

        val url = "http://localhost:$port/API/ticketing/update"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun updateTicketWrongClosedToResolved(){
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

        val url = "http://localhost:$port/API/ticketing/update"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun updateTicketWrongInProgressToReopened(){
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

        val url = "http://localhost:$port/API/ticketing/update"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun updateTicketWrongReopenedToOpen(){
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

        val url = "http://localhost:$port/API/ticketing/update"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun updateTicketWrongResolvedToInProgress(){
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

        val url = "http://localhost:$port/API/ticketing/update"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun updateTicketWrongResolvedToOpen(){
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

        val url = "http://localhost:$port/API/ticketing/update"
        val uri = URI(url)

        val requestEntity : HttpEntity<TicketUpdateDTO> = HttpEntity(ticketUpdate)
        val result = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }
}

