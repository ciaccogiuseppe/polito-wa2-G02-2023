package it.polito.wa2.server


import it.polito.wa2.server.products.Product
import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileRole
import it.polito.wa2.server.ticketing.attachment.Attachment
import it.polito.wa2.server.ticketing.attachment.AttachmentRepository
import it.polito.wa2.server.ticketing.message.Message
import it.polito.wa2.server.ticketing.message.MessageRepository
import it.polito.wa2.server.ticketing.ticket.Ticket
import it.polito.wa2.server.ticketing.ticket.TicketRepository
import it.polito.wa2.server.ticketing.ticket.TicketStatus
import jakarta.validation.constraints.NotBlank
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
import java.util.*


@Testcontainers
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
class AttachmentControllerTests {
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
    lateinit var attachmentRepository: AttachmentRepository
    @Autowired
    lateinit var profileRepository: ProfileRepository
    @Autowired
    lateinit var ticketRepository: TicketRepository
    @Autowired
    lateinit var productRepository: ProductRepository
    @Autowired
    lateinit var messageRepository: MessageRepository
    @Test
    @DirtiesContext
    fun getExistingAttachment() {
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

        val message = Message()
        message.text = "Test message"
        message.sentTimestamp = Timestamp(0)
        message.ticket = ticket
        message.sender = customer

        messageRepository.save(message)

        val attachment = Attachment()
        attachment.name = "image01.jpg"
        attachment.attachment = byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4e.toByte(), 0x47.toByte(), 0x0d.toByte(), 0x0a.toByte(), 0x1a.toByte(), 0x0a.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x0d.toByte(), 0x49.toByte(), 0x48.toByte(), 0x44.toByte(), 0x52.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x64.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x64.toByte(), 0x04.toByte(), 0x03.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x82.toByte(), 0xcc.toByte(), 0x88.toByte(), 0x67.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x01.toByte(), 0x73.toByte(), 0x52.toByte(), 0x47.toByte(), 0x42.toByte(), 0x00.toByte(), 0xae.toByte(), 0xce.toByte(), 0x1c.toByte(), 0xe9.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x04.toByte(), 0x67.toByte(), 0x41.toByte(), 0x4d.toByte(), 0x41.toByte(), 0x00.toByte(), 0x00.toByte(), 0xb1.toByte(), 0x8f.toByte(), 0x0b.toByte(), 0xfc.toByte(), 0x61.toByte(), 0x05.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x06.toByte(), 0x50.toByte(), 0x4c.toByte(), 0x54.toByte(), 0x45.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x55.toByte(), 0xc2.toByte(), 0xd3.toByte(), 0x7e.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x09.toByte(), 0x70.toByte(), 0x48.toByte(), 0x59.toByte(), 0x73.toByte(), 0x00.toByte(), 0x00.toByte(), 0x0e.toByte(), 0xc3.toByte(), 0x00.toByte(), 0x00.toByte(), 0x0e.toByte(), 0xc3.toByte(), 0x01.toByte(), 0xc7.toByte(), 0x6f.toByte(), 0xa8.toByte(), 0x64.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x54.toByte(), 0x49.toByte(), 0x44.toByte(), 0x41.toByte(), 0x54.toByte(), 0x58.toByte(), 0xc3.toByte(), 0xed.toByte(), 0xd3.toByte(), 0x41.toByte(), 0x0e.toByte(), 0x80.toByte(), 0x20.toByte(), 0x0c.toByte(), 0x44.toByte(), 0xd1.toByte(), 0xe9.toByte(), 0x0d.toByte(), 0xf0.toByte(), 0xfe.toByte(), 0x97.toByte(), 0x25.toByte(), 0xda.toByte(), 0xc2.toByte(), 0x42.toByte(), 0x8d.toByte(), 0x61.toByte(), 0x45.toByte(), 0x26.toByte(), 0xe6.toByte(), 0xbf.toByte(), 0x05.toByte(), 0x94.toByte(), 0x36.toByte(), 0xb3.toByte(), 0x02.toByte(), 0x04.toByte(), 0x00.toByte(), 0x00.toByte(), 0x8c.toByte(), 0x1c.toByte(), 0x45.toByte(), 0x51.toByte(), 0x45.toByte(), 0x53.toByte(), 0x96.toByte(), 0x63.toByte(), 0xcf.toByte(), 0x72.toByte(), 0x7f.toByte(), 0xe4.toByte(), 0x4a.toByte(), 0x9d.toByte(), 0x6b.toByte(), 0xcc.toByte(), 0x49.toByte(), 0x9e.toByte(), 0xef.toByte(), 0x5d.toByte(), 0x9b.toByte(), 0x48.toByte(), 0x22.toByte(), 0xf2.toByte(), 0x15.toByte(), 0x79.toByte(), 0xdc.toByte(), 0xbe.toByte(), 0x4b.toByte(), 0xa4.toByte(), 0x2d.toByte(), 0x74.toByte(), 0x89.toByte(), 0xbc.toByte(), 0x0d.toByte(), 0xed.toByte(), 0x7e.toByte(), 0xa5.toByte(), 0xdd.toByte(), 0x1b.toByte(), 0x03.toByte(), 0x00.toByte(), 0xe0.toByte(), 0xef.toByte(), 0xa4.toByte(), 0x0e.toByte(), 0x34.toByte(), 0x24.toByte(), 0x16.toByte(), 0xea.toByte(), 0x92.toByte(), 0x7e.toByte(), 0x66.toByte(), 0x58.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x49.toByte(), 0x45.toByte(), 0x4e.toByte(), 0x44.toByte(), 0xae.toByte(), 0x42.toByte(), 0x60)
        attachment.message = message

        attachmentRepository.save(attachment)

        val url = "http://localhost:$port/API/attachment/${attachment.attachmentId}"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        val body = json.parseMap(result.body)
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        Assertions.assertEquals(attachment.name, body["name"])
        Assertions.assertEquals(Base64.getEncoder().encodeToString(attachment.attachment), body["attachment"])
        Assertions.assertEquals(attachment.attachmentId, body["attachmentId"] )


        attachmentRepository.delete(attachment)
        messageRepository.delete(message)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)

    }

    @Test
    @DirtiesContext
    fun getWrongIdAttachment() {

        val url = "http://localhost:$port/API/attachment/_abc"
        val uri = URI(url)

        val result = restTemplate.getForEntity(uri, String::class.java)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }

    @Test
    @DirtiesContext
    fun getNegativeIdAttachment() {

        val url = "http://localhost:$port/API/attachment/-1"
        val uri = URI(url)

        val result = restTemplate.getForEntity(uri, String::class.java)
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.statusCode)
    }

    @Test
    @DirtiesContext
    fun getNonExistingAttachment() {

        val url = "http://localhost:$port/API/attachment/1"
        val uri = URI(url)

        val result = restTemplate.getForEntity(uri, String::class.java)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

    }

}

