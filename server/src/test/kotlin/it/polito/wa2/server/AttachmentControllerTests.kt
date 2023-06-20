package it.polito.wa2.server


import dasniko.testcontainers.keycloak.KeycloakContainer
import it.polito.wa2.server.brands.Brand
import it.polito.wa2.server.brands.BrandRepository
import it.polito.wa2.server.categories.Category
import it.polito.wa2.server.categories.CategoryRepository
import it.polito.wa2.server.categories.ProductCategory
import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileRole
import it.polito.wa2.server.ticketing.attachment.AttachmentRepository
import it.polito.wa2.server.ticketing.message.MessageRepository
import it.polito.wa2.server.ticketing.ticket.TicketRepository
import it.polito.wa2.server.ticketing.ticket.TicketStatus
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
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
class AttachmentControllerTests {
    val imageArray = byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4e.toByte(), 0x47.toByte(), 0x0d.toByte(), 0x0a.toByte(), 0x1a.toByte(), 0x0a.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x0d.toByte(), 0x49.toByte(), 0x48.toByte(), 0x44.toByte(), 0x52.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x64.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x64.toByte(), 0x04.toByte(), 0x03.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x82.toByte(), 0xcc.toByte(), 0x88.toByte(), 0x67.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x01.toByte(), 0x73.toByte(), 0x52.toByte(), 0x47.toByte(), 0x42.toByte(), 0x00.toByte(), 0xae.toByte(), 0xce.toByte(), 0x1c.toByte(), 0xe9.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x04.toByte(), 0x67.toByte(), 0x41.toByte(), 0x4d.toByte(), 0x41.toByte(), 0x00.toByte(), 0x00.toByte(), 0xb1.toByte(), 0x8f.toByte(), 0x0b.toByte(), 0xfc.toByte(), 0x61.toByte(), 0x05.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x06.toByte(), 0x50.toByte(), 0x4c.toByte(), 0x54.toByte(), 0x45.toByte(), 0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x55.toByte(), 0xc2.toByte(), 0xd3.toByte(), 0x7e.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x09.toByte(), 0x70.toByte(), 0x48.toByte(), 0x59.toByte(), 0x73.toByte(), 0x00.toByte(), 0x00.toByte(), 0x0e.toByte(), 0xc3.toByte(), 0x00.toByte(), 0x00.toByte(), 0x0e.toByte(), 0xc3.toByte(), 0x01.toByte(), 0xc7.toByte(), 0x6f.toByte(), 0xa8.toByte(), 0x64.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x54.toByte(), 0x49.toByte(), 0x44.toByte(), 0x41.toByte(), 0x54.toByte(), 0x58.toByte(), 0xc3.toByte(), 0xed.toByte(), 0xd3.toByte(), 0x41.toByte(), 0x0e.toByte(), 0x80.toByte(), 0x20.toByte(), 0x0c.toByte(), 0x44.toByte(), 0xd1.toByte(), 0xe9.toByte(), 0x0d.toByte(), 0xf0.toByte(), 0xfe.toByte(), 0x97.toByte(), 0x25.toByte(), 0xda.toByte(), 0xc2.toByte(), 0x42.toByte(), 0x8d.toByte(), 0x61.toByte(), 0x45.toByte(), 0x26.toByte(), 0xe6.toByte(), 0xbf.toByte(), 0x05.toByte(), 0x94.toByte(), 0x36.toByte(), 0xb3.toByte(), 0x02.toByte(), 0x04.toByte(), 0x00.toByte(), 0x00.toByte(), 0x8c.toByte(), 0x1c.toByte(), 0x45.toByte(), 0x51.toByte(), 0x45.toByte(), 0x53.toByte(), 0x96.toByte(), 0x63.toByte(), 0xcf.toByte(), 0x72.toByte(), 0x7f.toByte(), 0xe4.toByte(), 0x4a.toByte(), 0x9d.toByte(), 0x6b.toByte(), 0xcc.toByte(), 0x49.toByte(), 0x9e.toByte(), 0xef.toByte(), 0x5d.toByte(), 0x9b.toByte(), 0x48.toByte(), 0x22.toByte(), 0xf2.toByte(), 0x15.toByte(), 0x79.toByte(), 0xdc.toByte(), 0xbe.toByte(), 0x4b.toByte(), 0xa4.toByte(), 0x2d.toByte(), 0x74.toByte(), 0x89.toByte(), 0xbc.toByte(), 0x0d.toByte(), 0xed.toByte(), 0x7e.toByte(), 0xa5.toByte(), 0xdd.toByte(), 0x1b.toByte(), 0x03.toByte(), 0x00.toByte(), 0xe0.toByte(), 0xef.toByte(), 0xa4.toByte(), 0x0e.toByte(), 0x34.toByte(), 0x24.toByte(), 0x16.toByte(), 0xea.toByte(), 0x92.toByte(), 0x7e.toByte(), 0x66.toByte(), 0x58.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x49.toByte(), 0x45.toByte(), 0x4e.toByte(), 0x44.toByte(), 0xae.toByte(), 0x42.toByte(), 0x60)

    val json = BasicJsonParser()
    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:latest")

        @Container
        val keycloak: KeycloakContainer = KeycloakContainer().withRealmImportFile("keycloak/realm-test.json")

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
        @AfterAll
        fun clean(){
            keycloak.stop()
            postgres.close()
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") {"create-drop"}
            registry.add("spring.datasource.hikari.validation-timeout"){"250"}
            registry.add("spring.datasource.hikari.connection-timeout"){"250"}
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri")
            {keycloak.authServerUrl + "realms/SpringBootKeycloak"}
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
    @Autowired
    lateinit var brandRepository: BrandRepository
    @Autowired
    lateinit var categoryRepository: CategoryRepository


    @Test
    @DirtiesContext
    fun getExistingAttachmentUnauthorized() {
        val customer = TestUtils.testProfile("mario.rossi@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)

        val brand = Brand()
        brand.name = "Apple"

        brandRepository.save(brand)

        val category = Category()
        category.name=ProductCategory.SMARTPHONE
        categoryRepository.save(category)
val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)

        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val message = TestUtils.testMessage("Test message", Timestamp(0), ticket, customer)
        messageRepository.save(message)

        val attachment = TestUtils.testAttachment("image01.jpg", imageArray, message)
        attachmentRepository.save(attachment)

        val uri = URI("http://localhost:$port/API/attachment/${attachment.getId()}")

        val entity = TestUtils.testEntityHeader(null, "")

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)

        attachmentRepository.delete(attachment)
        messageRepository.delete(message)
        ticketRepository.delete(ticket)
        profileRepository.delete(manager)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)

        brandRepository.delete(brand)
        productRepository.delete(product)
    }

    @Test
    //@DirtiesContext
    fun getExistingAttachmentManager() {

        val customer = TestUtils.testProfile("mario.rossi@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)


        val brand = Brand()
        brand.name = "Apple"
        brandRepository.save(brand)

        val category = Category()
        category.name=ProductCategory.SMARTPHONE
        categoryRepository.save(category)
val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)

        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val message = TestUtils.testMessage("Test message", Timestamp(0), ticket, customer)
        messageRepository.save(message)

        val attachment = TestUtils.testAttachment("image01.jpg", imageArray, message)
        attachmentRepository.save(attachment)

        val uri = URI("http://localhost:$port/API/manager/attachment/${attachment.getId()}")

        val entity = TestUtils.testEntityHeader(null, managerToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseMap(result.body)
        Assertions.assertEquals(attachment.name, body["name"])
        Assertions.assertEquals(Base64.getEncoder().encodeToString(attachment.attachment), body["attachment"])
        Assertions.assertEquals(attachment.getId(), body["attachmentId"] )

        attachmentRepository.delete(attachment)
        messageRepository.delete(message)
        ticketRepository.delete(ticket)
        profileRepository.delete(manager)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        brandRepository.delete(brand)
        productRepository.delete(product)
    }

    @Test
    // @DirtiesContext
    fun getExistingAttachmentAuthorizedClient() {

        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)
        val brand = Brand()
        brand.name = "Apple"

        brandRepository.save(brand)
        val category = Category()
        category.name=ProductCategory.SMARTPHONE
        categoryRepository.save(category)
val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)

        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val message = TestUtils.testMessage("Test message", Timestamp(0), ticket, customer)
        messageRepository.save(message)

        val attachment = TestUtils.testAttachment("image01.jpg", imageArray, message)
        attachmentRepository.save(attachment)

        val uri = URI("http://localhost:$port/API/attachment/${attachment.getId()}")

        val entity = TestUtils.testEntityHeader(null, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseMap(result.body)
        Assertions.assertEquals(attachment.name, body["name"])
        Assertions.assertEquals(Base64.getEncoder().encodeToString(attachment.attachment), body["attachment"])
        Assertions.assertEquals(attachment.getId(), body["attachmentId"] )


        attachmentRepository.delete(attachment)
        messageRepository.delete(message)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(manager)
        profileRepository.delete(expert)
        productRepository.delete(product)
        brandRepository.delete(brand)
        productRepository.delete(product)
    }

    @Test
    //@DirtiesContext
    fun getExistingAttachmentForbiddenClient() {

        val customer = TestUtils.testProfile("mario.rossi@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)
        val brand = Brand()
        brand.name = "Apple"

        brandRepository.save(brand)
        val category = Category()
        category.name=ProductCategory.SMARTPHONE
        categoryRepository.save(category)
val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)

        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val message = TestUtils.testMessage("Test message", Timestamp(0), ticket, customer)
        messageRepository.save(message)

        val attachment = TestUtils.testAttachment("image01.jpg", imageArray, message)
        attachmentRepository.save(attachment)

        val uri = URI("http://localhost:$port/API/attachment/${attachment.getId()}")

        val entity = TestUtils.testEntityHeader(null, clientToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)


        attachmentRepository.delete(attachment)
        messageRepository.delete(message)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(manager)
        profileRepository.delete(expert)
        productRepository.delete(product)
        brandRepository.delete(brand)
        productRepository.delete(product)
    }

    @Test
    //@DirtiesContext
    fun getExistingAttachmentAuthorizedExpert() {

        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("expert@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)
        val brand = Brand()
        brand.name = "Apple"

        brandRepository.save(brand)
        val category = Category()
        category.name=ProductCategory.SMARTPHONE
        categoryRepository.save(category)
val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)

        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val message = TestUtils.testMessage("Test message", Timestamp(0), ticket, customer)
        messageRepository.save(message)

        val attachment = TestUtils.testAttachment("image01.jpg", imageArray, message)
        attachmentRepository.save(attachment)

        val uri = URI("http://localhost:$port/API/attachment/${attachment.getId()}")

        val entity = TestUtils.testEntityHeader(null, expertToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )


        Assertions.assertEquals(HttpStatus.OK, result.statusCode)
        val body = json.parseMap(result.body)
        Assertions.assertEquals(attachment.name, body["name"])
        Assertions.assertEquals(Base64.getEncoder().encodeToString(attachment.attachment), body["attachment"])
        Assertions.assertEquals(attachment.getId(), body["attachmentId"] )


        attachmentRepository.delete(attachment)
        messageRepository.delete(message)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
        brandRepository.delete(brand)
        productRepository.delete(product)
    }

    @Test
    //@DirtiesContext
    fun getExistingAttachmentForbiddenExpert() {

        val customer = TestUtils.testProfile("client@polito.it", "Mario", "Rossi", ProfileRole.CLIENT)
        val expert = TestUtils.testProfile("mario.bianchi@polito.it", "Mario", "Bianchi", ProfileRole.EXPERT)
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)
        profileRepository.save(customer)
        profileRepository.save(expert)
        val brand = Brand()
        brand.name = "Apple"

        brandRepository.save(brand)

        val category = Category()
        category.name=ProductCategory.SMARTPHONE
        categoryRepository.save(category)
val product = TestUtils.testProduct("0000000000000", "PC Omen Intel i7", brand,category)

        productRepository.save(product)

        val ticket = TestUtils.testTicket(Timestamp(0), product, customer, TicketStatus.IN_PROGRESS, expert, 2, "Ticket sample", "Ticket description sample")
        ticketRepository.save(ticket)

        val message = TestUtils.testMessage("Test message", Timestamp(0), ticket, customer)
        messageRepository.save(message)

        val attachment = TestUtils.testAttachment("image01.jpg", imageArray, message)
        attachmentRepository.save(attachment)

        val uri = URI("http://localhost:$port/API/attachment/${attachment.getId()}")

        val entity = TestUtils.testEntityHeader(null, expertToken)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)


        attachmentRepository.delete(attachment)
        messageRepository.delete(message)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
        brandRepository.delete(brand)
        productRepository.delete(product)
    }

    @Test
    //@DirtiesContext
    fun getWrongIdAttachment() {

        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)

        val uri = URI("http://localhost:$port/API/manager/attachment/_abc")

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
    fun getNegativeIdAttachment() {
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)


        val uri = URI("http://localhost:$port/API/manager/attachment/-1")

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
    //@DirtiesContext
    fun getNonExistingAttachment() {
        val manager = TestUtils.testProfile("manager@polito.it", "Manager", "Polito", ProfileRole.MANAGER)
        profileRepository.save(manager)

        val uri = URI("http://localhost:$port/API/manager/attachment/1")

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

}

