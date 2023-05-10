package it.polito.wa2.server


import it.polito.wa2.server.products.Product
import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileDTO
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.ticketing.attachment.Attachment
import it.polito.wa2.server.ticketing.attachment.AttachmentRepository
import it.polito.wa2.server.ticketing.message.Message
import it.polito.wa2.server.ticketing.message.MessageDTO
import it.polito.wa2.server.ticketing.message.MessageRepository
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
class MessageControllerTests {
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
    fun getExistingMessages() {
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"

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

        val ticket2 = Ticket()
        ticket2.createdTimestamp = Timestamp(1)
        ticket2.product = product
        ticket2.customer = customer
        ticket2.status = TicketStatus.IN_PROGRESS
        ticket2.expert = expert
        ticket2.priority = 2
        ticket2.title = "Ticket sample"
        ticket2.description = "Ticket description sample"

        ticketRepository.save(ticket)
        ticketRepository.save(ticket2)

        val message1 = Message()
        message1.text = "Test message"
        message1.sentTimestamp = Timestamp(0)
        message1.ticket = ticket
        message1.sender = customer

        val message2 = Message()
        message2.text = "Test message 2"
        message2.sentTimestamp = Timestamp(0)
        message2.ticket = ticket
        message2.sender = expert

        val message3 = Message()
        message3.text = "Test message 3"
        message3.sentTimestamp = Timestamp(0)
        message3.ticket = ticket
        message3.sender = expert

        val message4 = Message()
        message4.text = "Test message 4"
        message4.sentTimestamp = Timestamp(0)
        message4.ticket = ticket2
        message4.sender = expert

        messageRepository.save(message1)
        messageRepository.save(message2)
        messageRepository.save(message3)
        messageRepository.save(message4)

        val url = "http://localhost:$port/API/chat/${ticket.ticketId}"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(3, body.size)
        Assertions.assertEquals(true, body.any{a -> a["text"] == message1.text})
        Assertions.assertEquals(true, body.any{a -> a["text"] == message2.text})
        Assertions.assertEquals(true, body.any{a -> a["text"] == message3.text})

        Assertions.assertEquals(true, body.any{a -> a["messageId"] == message1.messageId})
        Assertions.assertEquals(true, body.any{a -> a["messageId"] == message2.messageId})
        Assertions.assertEquals(true, body.any{a -> a["messageId"] == message3.messageId})


        Assertions.assertEquals(true, body.all{a -> a["ticketId"] == ticket.ticketId})

        Assertions.assertEquals(true, body.any{a -> a["senderId"] == customer.email})
        Assertions.assertEquals(true, body.any{a -> a["senderId"] == expert.email})

        messageRepository.delete(message4)
        messageRepository.delete(message3)
        messageRepository.delete(message2)
        messageRepository.delete(message1)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun getEmptyChat() {
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"

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

        val ticket2 = Ticket()
        ticket2.createdTimestamp = Timestamp(1)
        ticket2.product = product
        ticket2.customer = customer
        ticket2.status = TicketStatus.IN_PROGRESS
        ticket2.expert = expert
        ticket2.priority = 2
        ticket2.title = "Ticket sample"
        ticket2.description = "Ticket description sample"

        ticketRepository.save(ticket)
        ticketRepository.save(ticket2)

        val message1 = Message()
        message1.text = "Test message"
        message1.sentTimestamp = Timestamp(0)
        message1.ticket = ticket
        message1.sender = customer

        val message2 = Message()
        message2.text = "Test message 2"
        message2.sentTimestamp = Timestamp(0)
        message2.ticket = ticket
        message2.sender = expert

        val message3 = Message()
        message3.text = "Test message 3"
        message3.sentTimestamp = Timestamp(0)
        message3.ticket = ticket
        message3.sender = expert

        messageRepository.save(message1)
        messageRepository.save(message2)
        messageRepository.save(message3)

        val url = "http://localhost:$port/API/chat/${ticket2.ticketId}"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(0, body.size)
        messageRepository.delete(message3)
        messageRepository.delete(message2)
        messageRepository.delete(message1)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun getNonExistingChat() {

        val url = "http://localhost:$port/API/chat/1"
        val uri = URI(url)

        val result = restTemplate.getForEntity(uri, String::class.java)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
    }

    @Test
    @DirtiesContext
    fun getWrongIdChat() {

        val url = "http://localhost:$port/API/chat/abc"
        val uri = URI(url)

        val result = restTemplate.getForEntity(uri, String::class.java)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
    }

    @Test
    @DirtiesContext
    fun getChatWithAttachments() {
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"

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

        val ticket2 = Ticket()
        ticket2.createdTimestamp = Timestamp(1)
        ticket2.product = product
        ticket2.customer = customer
        ticket2.status = TicketStatus.IN_PROGRESS
        ticket2.expert = expert
        ticket2.priority = 2
        ticket2.title = "Ticket sample"
        ticket2.description = "Ticket description sample"

        ticketRepository.save(ticket)
        ticketRepository.save(ticket2)

        val message1 = Message()
        message1.text = "Test message"
        message1.sentTimestamp = Timestamp(0)
        message1.ticket = ticket
        message1.sender = customer

        val message2 = Message()
        message2.text = "Test message 2"
        message2.sentTimestamp = Timestamp(0)
        message2.ticket = ticket
        message2.sender = expert

        val message3 = Message()
        message3.text = "Test message 3"
        message3.sentTimestamp = Timestamp(0)
        message3.ticket = ticket
        message3.sender = expert

        val message4 = Message()
        message4.text = "Test message 4"
        message4.sentTimestamp = Timestamp(0)
        message4.ticket = ticket2
        message4.sender = expert

        messageRepository.save(message1)
        messageRepository.save(message2)
        messageRepository.save(message3)
        messageRepository.save(message4)

        val attachment1 = Attachment()
        attachment1.name = "image01.jpg"
        attachment1.message = message1

        val attachment2 = Attachment()
        attachment2.name = "image02.jpg"
        attachment2.attachment = byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4e.toByte(), 0x47.toByte(), 0x0d.toByte(), 0x0a.toByte(), 0x1a.toByte(), 0x0a.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x0d.toByte(), 0x49.toByte(), 0x48.toByte(), 0x44.toByte(), 0x52.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x64.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x64.toByte(), 0x04.toByte(), 0x03.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x82.toByte(), 0xcc.toByte(), 0x88.toByte(), 0x67.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x01.toByte(), 0x73.toByte(), 0x52.toByte(), 0x47.toByte(), 0x42.toByte(), 0x00.toByte(), 0xae.toByte(), 0xce.toByte(), 0x1c.toByte(), 0xe9.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x04.toByte(), 0x67.toByte(), 0x41.toByte(), 0x4d.toByte(), 0x41.toByte(), 0x00.toByte(), 0x00.toByte(), 0xb1.toByte(), 0x8f.toByte(), 0x0b.toByte(), 0xfc.toByte(), 0x61.toByte(), 0x05.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x06.toByte(), 0x50.toByte(), 0x4c.toByte(), 0x54.toByte(), 0x45.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x55.toByte(), 0xc2.toByte(), 0xd3.toByte(), 0x7e.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x09.toByte(), 0x70.toByte(), 0x48.toByte(), 0x59.toByte(), 0x73.toByte(), 0x00.toByte(), 0x00.toByte(), 0x0e.toByte(), 0xc3.toByte(), 0x00.toByte(), 0x00.toByte(), 0x0e.toByte(), 0xc3.toByte(), 0x01.toByte(), 0xc7.toByte(), 0x6f.toByte(), 0xa8.toByte(), 0x64.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x54.toByte(), 0x49.toByte(), 0x44.toByte(), 0x41.toByte(), 0x54.toByte(), 0x58.toByte(), 0xc3.toByte(), 0xed.toByte(), 0xd3.toByte(), 0x41.toByte(), 0x0e.toByte(), 0x80.toByte(), 0x20.toByte(), 0x0c.toByte(), 0x44.toByte(), 0xd1.toByte(), 0xe9.toByte(), 0x0d.toByte(), 0xf0.toByte(), 0xfe.toByte(), 0x97.toByte(), 0x25.toByte(), 0xda.toByte(), 0xc2.toByte(), 0x42.toByte(), 0x8d.toByte(), 0x61.toByte(), 0x45.toByte(), 0x26.toByte(), 0xe6.toByte(), 0xbf.toByte(), 0x05.toByte(), 0x94.toByte(), 0x36.toByte(), 0xb3.toByte(), 0x02.toByte(), 0x04.toByte(), 0x00.toByte(), 0x00.toByte(), 0x8c.toByte(), 0x1c.toByte(), 0x45.toByte(), 0x51.toByte(), 0x45.toByte(), 0x53.toByte(), 0x96.toByte(), 0x63.toByte(), 0xcf.toByte(), 0x72.toByte(), 0x7f.toByte(), 0xe4.toByte(), 0x4a.toByte(), 0x9d.toByte(), 0x6b.toByte(), 0xcc.toByte(), 0x49.toByte(), 0x9e.toByte(), 0xef.toByte(), 0x5d.toByte(), 0x9b.toByte(), 0x48.toByte(), 0x22.toByte(), 0xf2.toByte(), 0x15.toByte(), 0x79.toByte(), 0xdc.toByte(), 0xbe.toByte(), 0x4b.toByte(), 0xa4.toByte(), 0x2d.toByte(), 0x74.toByte(), 0x89.toByte(), 0xbc.toByte(), 0x0d.toByte(), 0xed.toByte(), 0x7e.toByte(), 0xa5.toByte(), 0xdd.toByte(), 0x1b.toByte(), 0x03.toByte(), 0x00.toByte(), 0xe0.toByte(), 0xef.toByte(), 0xa4.toByte(), 0x0e.toByte(), 0x34.toByte(), 0x24.toByte(), 0x16.toByte(), 0xea.toByte(), 0x92.toByte(), 0x7e.toByte(), 0x66.toByte(), 0x58.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x49.toByte(), 0x45.toByte(), 0x4e.toByte(), 0x44.toByte(), 0xae.toByte(), 0x42.toByte(), 0x60)
        attachment2.message = message4

        attachmentRepository.save(attachment1)
        attachmentRepository.save(attachment2)

        val url = "http://localhost:$port/API/chat/${ticket2.ticketId}"
        val uri = URI(url)
        val json = BasicJsonParser()

        val result = restTemplate.getForEntity(uri, String::class.java)
        val body = json.parseList(result.body).map{it as LinkedHashMap<*,*>}
        Assertions.assertEquals(HttpStatus.OK, result.statusCode)

        Assertions.assertEquals(1, body.size)

        Assertions.assertEquals(
            attachment2.attachmentId,
            ((body[0]["attachments"]as ArrayList<*>)[0] as LinkedHashMap<*,*>)["attachmentId"]
            )

        attachmentRepository.delete(attachment1)
        attachmentRepository.delete(attachment2)
        messageRepository.delete(message4)
        messageRepository.delete(message3)
        messageRepository.delete(message2)
        messageRepository.delete(message1)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }


    @Test
    @DirtiesContext
    fun addMessageSuccessful() {
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"

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

        val ticket2 = Ticket()
        ticket2.createdTimestamp = Timestamp(1)
        ticket2.product = product
        ticket2.customer = customer
        ticket2.status = TicketStatus.IN_PROGRESS
        ticket2.expert = expert
        ticket2.priority = 2
        ticket2.title = "Ticket sample"
        ticket2.description = "Ticket description sample"

        ticketRepository.save(ticket)
        ticketRepository.save(ticket2)

        val message = MessageDTO(
            null,
            ticket.ticketId!!,
            customer.email,
            "message text",
            Timestamp(0),
            mutableSetOf()
        )

        val url = "http://localhost:$port/API/chat/${ticket.ticketId}"
        val uri = URI(url)
        val requestEntity : HttpEntity<MessageDTO> = HttpEntity(message)
        val result = restTemplate.postForEntity(uri, requestEntity, message.javaClass)

        Assertions.assertEquals(HttpStatus.CREATED, result.statusCode)

        val addedMessage = messageRepository.findAllByTicket(ticket)

        Assertions.assertEquals(1, addedMessage.size)
        Assertions.assertEquals(message.text, addedMessage[0].text)
        Assertions.assertEquals(1, addedMessage[0].messageId)

        messageRepository.delete(addedMessage[0])
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun addMessageNonExistingTicket() {
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"

        profileRepository.save(customer)
        profileRepository.save(expert)

        val product = Product()
        product.productId = "0000000000000"
        product.name = "PC Omen Intel i7"
        product.brand = "HP"

        productRepository.save(product)


        val message = MessageDTO(
            null,
            25,
            customer.email,
            "message text",
            Timestamp(0),
            mutableSetOf()
        )

        val url = "http://localhost:$port/API/chat/25"
        val uri = URI(url)
        val requestEntity : HttpEntity<MessageDTO> = HttpEntity(message)
        val result = restTemplate.postForEntity(uri, requestEntity, String.javaClass)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun addMessageNonExistingProfile() {
        val customer = Profile()
        customer.email = "mario.rossi@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"

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

        val message = MessageDTO(
            null,
            ticket.ticketId!!,
            "something@mail.it",
            "message text",
            Timestamp(0),
            mutableSetOf()
        )

        val url = "http://localhost:$port/API/chat/${ticket.ticketId}"
        val uri = URI(url)
        val requestEntity : HttpEntity<MessageDTO> = HttpEntity(message)
        val result = restTemplate.postForEntity(uri, requestEntity, String.javaClass)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }
}

