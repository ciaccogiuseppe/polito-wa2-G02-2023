package it.polito.wa2.server


import dasniko.testcontainers.keycloak.KeycloakContainer
import it.polito.wa2.server.products.Product
import it.polito.wa2.server.products.ProductRepository
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileRole
import it.polito.wa2.server.ticketing.attachment.Attachment
import it.polito.wa2.server.ticketing.attachment.AttachmentRepository
import it.polito.wa2.server.ticketing.message.Message
import it.polito.wa2.server.ticketing.message.MessageDTO
import it.polito.wa2.server.ticketing.message.MessageRepository
import it.polito.wa2.server.ticketing.ticket.Ticket
import it.polito.wa2.server.ticketing.ticket.TicketRepository
import it.polito.wa2.server.ticketing.ticket.TicketStatus
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
import java.util.*


@Testcontainers
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace=AutoConfigureTestDatabase.Replace.NONE)
class MessageControllerTests {
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

            val realmName = "SpringBootKeycloak"
            val clientId = "springboot-keycloak-client"

            val manager = UserRepresentation()
            manager.email = "manager@polito.it"
            manager.username = "manager_01"
            manager.isEnabled = true

            val client = UserRepresentation()
            client.email = "client@polito.it"
            client.username = "client_01"
            client.isEnabled = true

            val expert = UserRepresentation()
            expert.email = "expert@polito.it"
            expert.username = "expert_01"
            expert.isEnabled = true

            val credential = CredentialRepresentation()
            credential.isTemporary = false
            credential.type = CredentialRepresentation.PASSWORD
            credential.value = "password"

            keycloak.keycloakAdminClient.realm(realmName).users().create(manager)
            keycloak.keycloakAdminClient.realm(realmName).users().create(client)
            keycloak.keycloakAdminClient.realm(realmName).users().create(expert)


            val createdManager =
                keycloak.keycloakAdminClient.realm(realmName).users().search(manager.email)[0]
            val createdClient =
                keycloak.keycloakAdminClient.realm(realmName).users().search(client.email)[0]
            val createdExpert =
                keycloak.keycloakAdminClient.realm(realmName).users().search(expert.email)[0]

            val roleManager = keycloak.keycloakAdminClient.realm(realmName).roles().get("app_manager")
            val roleClient = keycloak.keycloakAdminClient.realm(realmName).roles().get("app_client")
            val roleExpert = keycloak.keycloakAdminClient.realm(realmName).roles().get("app_expert")

            keycloak.keycloakAdminClient.realm(realmName).users().get(createdManager.id).resetPassword(credential)
            keycloak.keycloakAdminClient.realm(realmName).users().get(createdManager.id).roles().realmLevel().add(listOf(roleManager.toRepresentation()))

            keycloak.keycloakAdminClient.realm(realmName).users().get(createdClient.id).resetPassword(credential)
            keycloak.keycloakAdminClient.realm(realmName).users().get(createdClient.id).roles().realmLevel().add(listOf(roleClient.toRepresentation()))

            keycloak.keycloakAdminClient.realm(realmName).users().get(createdExpert.id).resetPassword(credential)
            keycloak.keycloakAdminClient.realm(realmName).users().get(createdExpert.id).roles().realmLevel().add(listOf(roleExpert.toRepresentation()))


            val kcManager = KeycloakBuilder
                .builder()
                .serverUrl(keycloak.authServerUrl)
                .realm(realmName)
                .clientId(clientId)
                .username(manager.email)
                .password("password")
                .build()

            val kcClient = KeycloakBuilder
                .builder()
                .serverUrl(keycloak.authServerUrl)
                .realm(realmName)
                .clientId(clientId)
                .username(client.email)
                .password("password")
                .build()

            val kcExpert = KeycloakBuilder
                .builder()
                .serverUrl(keycloak.authServerUrl)
                .realm(realmName)
                .clientId(clientId)
                .username(expert.email)
                .password("password")
                .build()

            kcManager.tokenManager().grantToken().expiresIn = 3600
            kcClient.tokenManager().grantToken().expiresIn = 3600
            kcExpert.tokenManager().grantToken().expiresIn = 3600


            managerToken = kcManager.tokenManager().accessToken.token
            clientToken = kcClient.tokenManager().accessToken.token
            expertToken = kcExpert.tokenManager().accessToken.token
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
    fun getExistingMessagesManager() {
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

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        val customer2 = Profile()
        customer2.email = "client@polito.it"
        customer2.name = "Mario"
        customer2.surname = "Rossi"
        customer2.role = ProfileRole.CUSTOMER

        val expert2 = Profile()
        expert2.email = "expert@polito.it"
        expert2.name = "Mario"
        expert2.surname = "Bianchi"
        expert2.role = ProfileRole.EXPERT

        profileRepository.save(customer2)
        profileRepository.save(expert2)
        profileRepository.save(manager)
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

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val entity = HttpEntity(null, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

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

        Assertions.assertEquals(true, body.any{a -> a["senderId"] == customer.profileId})
        Assertions.assertEquals(true, body.any{a -> a["senderId"] == expert.profileId})

        messageRepository.delete(message4)
        messageRepository.delete(message3)
        messageRepository.delete(message2)
        messageRepository.delete(message1)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        profileRepository.delete(manager)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun getExistingMessagesAuthorizedClient() {
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER



        val expert2 = Profile()
        expert2.email = "expert@polito.it"
        expert2.name = "Mario"
        expert2.surname = "Bianchi"
        expert2.role = ProfileRole.EXPERT

        profileRepository.save(expert2)
        profileRepository.save(manager)
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

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)

        val entity = HttpEntity(null, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

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

        Assertions.assertEquals(true, body.any{a -> a["senderId"] == customer.profileId})
        Assertions.assertEquals(true, body.any{a -> a["senderId"] == expert.profileId})

        messageRepository.delete(message4)
        messageRepository.delete(message3)
        messageRepository.delete(message2)
        messageRepository.delete(message1)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        profileRepository.delete(manager)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun getExistingMessagesUnauthorizedClient() {
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



        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        val customer2 = Profile()
        customer2.email = "client@polito.it"
        customer2.name = "Mario"
        customer2.surname = "Rossi"
        customer2.role = ProfileRole.CUSTOMER

        val expert2 = Profile()
        expert2.email = "expert@polito.it"
        expert2.name = "Mario"
        expert2.surname = "Bianchi"
        expert2.role = ProfileRole.EXPERT

        profileRepository.save(customer2)
        profileRepository.save(expert2)
        profileRepository.save(manager)
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

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)

        val entity = HttpEntity(null, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )


        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)

        messageRepository.delete(message4)
        messageRepository.delete(message3)
        messageRepository.delete(message2)
        messageRepository.delete(message1)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        profileRepository.delete(manager)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun getExistingMessagesAuthorizedExpert() {
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
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

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(expertToken)

        val entity = HttpEntity(null, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

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

        Assertions.assertEquals(true, body.any{a -> a["senderId"] == customer.profileId})
        Assertions.assertEquals(true, body.any{a -> a["senderId"] == expert.profileId})

        messageRepository.delete(message4)
        messageRepository.delete(message3)
        messageRepository.delete(message2)
        messageRepository.delete(message1)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(manager)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun getExistingMessagesUnauthorizedExpert() {
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

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        val customer2 = Profile()
        customer2.email = "client@polito.it"
        customer2.name = "Mario"
        customer2.surname = "Rossi"
        customer2.role = ProfileRole.CUSTOMER

        val expert2 = Profile()
        expert2.email = "expert@polito.it"
        expert2.name = "Mario"
        expert2.surname = "Bianchi"
        expert2.role = ProfileRole.EXPERT

        profileRepository.save(customer2)
        profileRepository.save(expert2)
        profileRepository.save(manager)
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

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(expertToken)

        val entity = HttpEntity(null, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)

        messageRepository.delete(message4)
        messageRepository.delete(message3)
        messageRepository.delete(message2)
        messageRepository.delete(message1)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        profileRepository.delete(manager)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun getExistingMessagesUnauthorized() {
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

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        val customer2 = Profile()
        customer2.email = "client@polito.it"
        customer2.name = "Mario"
        customer2.surname = "Rossi"
        customer2.role = ProfileRole.CUSTOMER

        val expert2 = Profile()
        expert2.email = "expert@polito.it"
        expert2.name = "Mario"
        expert2.surname = "Bianchi"
        expert2.role = ProfileRole.EXPERT

        profileRepository.save(customer2)
        profileRepository.save(expert2)
        profileRepository.save(manager)
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

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth("")

        val entity = HttpEntity(null, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, result.statusCode)

        messageRepository.delete(message4)
        messageRepository.delete(message3)
        messageRepository.delete(message2)
        messageRepository.delete(message1)
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        profileRepository.delete(manager)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun getEmptyChat() {
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

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        val customer2 = Profile()
        customer2.email = "client@polito.it"
        customer2.name = "Mario"
        customer2.surname = "Rossi"
        customer2.role = ProfileRole.CUSTOMER

        val expert2 = Profile()
        expert2.email = "expert@polito.it"
        expert2.name = "Mario"
        expert2.surname = "Bianchi"
        expert2.role = ProfileRole.EXPERT

        profileRepository.save(customer2)
        profileRepository.save(expert2)
        profileRepository.save(manager)
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

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val entity = HttpEntity(null, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )

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
        profileRepository.delete(manager)
    }

    @Test
    @DirtiesContext
    fun getNonExistingChat() {
        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)

        val url = "http://localhost:$port/API/chat/1"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val entity = HttpEntity(null, headers)

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
    @DirtiesContext
    fun getWrongIdChat() {
        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)

        val url = "http://localhost:$port/API/chat/abc"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val entity = HttpEntity(null, headers)

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
    fun getChatWithAttachments() {
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

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)

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

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(managerToken)

        val entity = HttpEntity(null, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.GET,
            entity,
            String::class.java
        )
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
        profileRepository.delete(manager)
        productRepository.delete(product)
    }


    @Test
    @DirtiesContext
    fun addMessageSuccessfulAuthorizedClient() {

        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT
        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)

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
            customer.profileId!!,
            "message text",
            Timestamp(1),
            mutableSetOf()
        )


        val url = "http://localhost:$port/API/chat/${ticket.ticketId}"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)

        val requestEntity : HttpEntity<MessageDTO> = HttpEntity(message, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )

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
        profileRepository.delete(manager)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun addMessageForbiddenClient() {
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

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER


        val customer2 = Profile()
        customer2.email = "client@polito.it"
        customer2.name = "Mario"
        customer2.surname = "Rossi"
        customer2.role = ProfileRole.CUSTOMER

        val expert2 = Profile()
        expert2.email = "expert@polito.it"
        expert2.name = "Mario"
        expert2.surname = "Bianchi"
        expert2.role = ProfileRole.EXPERT

        profileRepository.save(customer2)
        profileRepository.save(expert2)
        profileRepository.save(manager)
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
            customer.profileId!!,
            "message text",
            Timestamp(1),
            mutableSetOf()
        )


        val url = "http://localhost:$port/API/chat/${ticket.ticketId}"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)

        val requestEntity : HttpEntity<MessageDTO> = HttpEntity(message, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(manager)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun addMessageSuccessfulAuthorizedExpert() {
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "expert@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
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
            customer.profileId!!,
            "message text",
            Timestamp(1),
            mutableSetOf()
        )


        val url = "http://localhost:$port/API/chat/${ticket.ticketId}"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(expertToken)

        val requestEntity : HttpEntity<MessageDTO> = HttpEntity(message, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.CREATED, result.statusCode)

        val addedMessage = messageRepository.findAllByTicket(ticket)

        Assertions.assertEquals(1, addedMessage.size)
        Assertions.assertEquals(message.text, addedMessage[0].text)
        Assertions.assertEquals(1, addedMessage[0].messageId)

        messageRepository.delete(addedMessage[0])
        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(manager)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun addMessageForbiddenExpert() {
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

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        val customer2 = Profile()
        customer2.email = "client@polito.it"
        customer2.name = "Mario"
        customer2.surname = "Rossi"
        customer2.role = ProfileRole.CUSTOMER

        val expert2 = Profile()
        expert2.email = "expert@polito.it"
        expert2.name = "Mario"
        expert2.surname = "Bianchi"
        expert2.role = ProfileRole.EXPERT

        profileRepository.save(customer2)
        profileRepository.save(expert2)
        profileRepository.save(manager)
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
            customer.profileId!!,
            "message text",
            Timestamp(1),
            mutableSetOf()
        )


        val url = "http://localhost:$port/API/chat/${ticket.ticketId}"
        val uri = URI(url)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)

        val requestEntity : HttpEntity<MessageDTO> = HttpEntity(message, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.FORBIDDEN, result.statusCode)

        ticketRepository.delete(ticket2)
        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(manager)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }

    @Test
    @DirtiesContext
    fun addMessageNonExistingTicket() {
        val customer = Profile()
        customer.email = "client@polito.it"
        customer.name = "Mario"
        customer.surname = "Rossi"
        customer.role = ProfileRole.CUSTOMER

        val expert = Profile()
        expert.email = "mario.bianchi@polito.it"
        expert.name = "Mario"
        expert.surname = "Bianchi"
        expert.role = ProfileRole.EXPERT

        val manager = Profile()
        manager.email = "manager@polito.it"
        manager.name = "Manager"
        manager.surname = "Polito"
        manager.role = ProfileRole.MANAGER

        profileRepository.save(manager)
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
            customer.profileId!!,
            "message text",
            Timestamp(0),
            mutableSetOf()
        )

        val url = "http://localhost:$port/API/chat/25"
        val uri = URI(url)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(clientToken)

        val requestEntity : HttpEntity<MessageDTO> = HttpEntity(message, headers)

        val result = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            requestEntity,
            String::class.java
        )

        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
        profileRepository.delete(manager)
    }

    /*@Test
    @DirtiesContext
    fun addMessageNonExistingProfile() {
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

        val message = MessageDTO(
            null,
            ticket.ticketId!!,
            123456,
            "message text",
            Timestamp(0),
            mutableSetOf()
        )

        val url = "http://localhost:$port/API/chat/${ticket.ticketId}"
        val uri = URI(url)
        val requestEntity : HttpEntity<MessageDTO> = HttpEntity(message)
        val result = restTemplate.postForEntity(uri, requestEntity, String::class.java)

        Assertions.assertEquals(HttpStatus.NOT_FOUND, result.statusCode)

        ticketRepository.delete(ticket)
        profileRepository.delete(customer)
        profileRepository.delete(expert)
        productRepository.delete(product)
    }*/
}

