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

    // --------------------------- no filters

    @Test
    @DirtiesContext
    fun getTicketHistoryWithNoFiltersAtAll() {
        val url = "http://localhost:$port/API/ticketing/history/filter"
        val uri = URI(url)

        val result = restTemplate.getForEntity(uri, String::class.java)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
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
    fun getTicketHistoryByTicketIdNegative() {
        val url = "http://localhost:$port/API/ticketing/history/filter?ticketId=-3"
        val uri = URI(url)

        val result = restTemplate.getForEntity(uri, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryByTicketIdAlphabetic() {
        val url = "http://localhost:$port/API/ticketing/history/filter?ticketId=a"
        val uri = URI(url)

        val result = restTemplate.getForEntity(uri, String::class.java)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }

    // --------------------------- userId

    @Test
    @DirtiesContext
    fun getExistingTicketHistoryByUserId() {
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

        val url = "http://localhost:$port/API/ticketing/history/filter?userId=2"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}

        Assertions.assertEquals(body.size, 2)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(body[0]["historyId"], history2customer2.historyId)
        Assertions.assertEquals(body[0]["ticketId"], history2customer2.ticket!!.ticketId)
        Assertions.assertEquals(body[0]["newState"], history2customer2.newState.name)
        Assertions.assertEquals(body[0]["oldState"], history2customer2.oldState.name)
        Assertions.assertEquals(body[0]["currentExpertId"], history2customer2.currentExpert!!.profileId)
        Assertions.assertEquals(body[0]["userId"], history2customer2.user!!.profileId)
        Assertions.assertTrue(body[0]["updatedTimestamp"].toString().startsWith(history2customer2.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        Assertions.assertEquals(body[1]["historyId"], history3customer2.historyId)
        Assertions.assertEquals(body[1]["ticketId"], history3customer2.ticket!!.ticketId)
        Assertions.assertEquals(body[1]["newState"], history3customer2.newState.name)
        Assertions.assertEquals(body[1]["oldState"], history3customer2.oldState.name)
        Assertions.assertEquals(body[1]["currentExpertId"], history3customer2.currentExpert!!.profileId)
        Assertions.assertEquals(body[1]["userId"], history3customer2.user!!.profileId)
        Assertions.assertTrue(body[1]["updatedTimestamp"].toString().startsWith(history3customer2.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        ticketHistoryRepository.delete(history1customer1)
        ticketHistoryRepository.delete(history2customer2)
        ticketHistoryRepository.delete(history3customer2)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer1)
        profileRepository.delete(customer2)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun getNonExistingTicketHistoryByUserId() {
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

        val url = "http://localhost:$port/API/ticketing/history/filter?userId=3"
        val uri = URI(url)

        val result = restTemplate.getForEntity(uri, String::class.java)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)


        ticketHistoryRepository.delete(history1customer1)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer1)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryByUserIdNegative() {
        val url = "http://localhost:$port/API/ticketing/history/filter?userId=-3"
        val uri = URI(url)

        val result = restTemplate.getForEntity(uri, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryByUserIdAlphabetic() {
        val url = "http://localhost:$port/API/ticketing/history/filter?userId=a"
        val uri = URI(url)

        val result = restTemplate.getForEntity(uri, String::class.java)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }


    // --------------------------- currentExpertId

    @Test
    @DirtiesContext
    fun getExistingTicketHistoryByCurrentExpertId() {
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

        val url = "http://localhost:$port/API/ticketing/history/filter?currentExpertId=3"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}

        Assertions.assertEquals(body.size, 2)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(body[0]["historyId"], history2expert3.historyId)
        Assertions.assertEquals(body[0]["ticketId"], history2expert3.ticket!!.ticketId)
        Assertions.assertEquals(body[0]["newState"], history2expert3.newState.name)
        Assertions.assertEquals(body[0]["oldState"], history2expert3.oldState.name)
        Assertions.assertEquals(body[0]["currentExpertId"], history2expert3.currentExpert!!.profileId)
        Assertions.assertEquals(body[0]["userId"], history2expert3.user!!.profileId)
        Assertions.assertTrue(body[0]["updatedTimestamp"].toString().startsWith(history2expert3.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        Assertions.assertEquals(body[1]["historyId"], history3expert3.historyId)
        Assertions.assertEquals(body[1]["ticketId"], history3expert3.ticket!!.ticketId)
        Assertions.assertEquals(body[1]["newState"], history3expert3.newState.name)
        Assertions.assertEquals(body[1]["oldState"], history3expert3.oldState.name)
        Assertions.assertEquals(body[1]["currentExpertId"], history3expert3.currentExpert!!.profileId)
        Assertions.assertEquals(body[1]["userId"], history3expert3.user!!.profileId)
        Assertions.assertTrue(body[1]["updatedTimestamp"].toString().startsWith(history3expert3.updatedTimestamp!!.toInstant().toString().replace("Z", "")))

        ticketHistoryRepository.delete(history1expert2)
        ticketHistoryRepository.delete(history2expert3)
        ticketHistoryRepository.delete(history3expert3)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer1)
        profileRepository.delete(expert2)
        profileRepository.delete(expert3)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun getNonExistingTicketHistoryByCurrentExpertId() {
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

        val url = "http://localhost:$port/API/ticketing/history/filter?currentExpertId=3"
        val uri = URI(url)

        val result = restTemplate.getForEntity(uri, String::class.java)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)


        ticketHistoryRepository.delete(history1customer1)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer1)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryByCurrentExpertIdNegative() {
        val url = "http://localhost:$port/API/ticketing/history/filter?currentExpertId=-3"
        val uri = URI(url)

        val result = restTemplate.getForEntity(uri, String::class.java)

        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)
    }

    @Test
    @DirtiesContext
    fun getTicketHistoryByCurrentExpertIdAlphabetic() {
        val url = "http://localhost:$port/API/ticketing/history/filter?currentExpertId=a"
        val uri = URI(url)

        val result = restTemplate.getForEntity(uri, String::class.java)

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }

    // --------------------------- updatedAfter ?createdAfter=${Timestamp(0).toLocalDateTime()}&createdBefore=${Timestamp(1).toLocalDateTime()}

    // --------------------------- updatedBefore

    // --------------------------- all filters
}