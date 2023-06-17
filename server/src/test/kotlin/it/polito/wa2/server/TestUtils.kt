package it.polito.wa2.server

import dasniko.testcontainers.keycloak.KeycloakContainer
import it.polito.wa2.server.brands.Brand
import it.polito.wa2.server.categories.Category
import it.polito.wa2.server.products.Product
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileRole
import it.polito.wa2.server.ticketing.attachment.Attachment
import it.polito.wa2.server.ticketing.message.Message
import it.polito.wa2.server.ticketing.ticket.Ticket
import it.polito.wa2.server.ticketing.ticket.TicketStatus
import it.polito.wa2.server.ticketing.tickethistory.TicketHistory
import org.keycloak.admin.client.KeycloakBuilder
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.sql.Timestamp

class TestUtils {
    companion object {
        fun testProfile(email: String, name:String, surname:String, role:ProfileRole) : Profile {
            val profile = Profile()
            profile.email = email
            profile.name = name
            profile.surname = surname
            profile.role = role

            return profile
        }

        fun testProduct(productId: String, name:String, brand:Brand, category:Category) : Product {


            val product = Product()
            product.productId = productId
            product.name = name
            product.brand = brand
            product.category = category

            return product
        }

        fun testTicket(createdTimestamp:Timestamp, product:Product, customer:Profile, status:TicketStatus, expert:Profile, priority:Int, title:String, description:String) : Ticket{
            val ticket = Ticket()
            ticket.product = product
            ticket.customer = customer
            ticket.status = status
            ticket.expert = expert
            ticket.priority = priority
            ticket.title = title
            ticket.description = description
            ticket.createdTimestamp = createdTimestamp

            return ticket
        }

        fun testMessage(text:String, sentTimestamp: Timestamp, ticket:Ticket, sender: Profile) : Message{
            val message = Message()
            message.text = text
            message.sentTimestamp = sentTimestamp
            message.ticket = ticket
            message.sender = sender

            return message
        }

        fun testAttachment(name:String, data:ByteArray, message:Message):Attachment{
            val attachment = Attachment()
            attachment.name = name
            attachment.attachment = data
            attachment.message = message

            return attachment
        }

        fun testTicketHistory(ticket:Ticket, expert:Profile, newStatus: TicketStatus, oldStatus: TicketStatus, updatedTimestamp: Timestamp, user:Profile) : TicketHistory{
            val ticketHistory = TicketHistory()
            ticketHistory.ticket = ticket
            ticketHistory.currentExpert = expert
            ticketHistory.newState = newStatus
            ticketHistory.oldState = oldStatus
            ticketHistory.updatedTimestamp = updatedTimestamp
            ticketHistory.user = user
            return ticketHistory
        }


        fun <T> testEntityHeader(body: T?, token: String): HttpEntity<T> {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers.setBearerAuth(token)

            return HttpEntity(body, headers)
        }

        fun testKeycloakSetup(keycloak:KeycloakContainer) {

            val realmName = "SpringBootKeycloak"

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

        }

        fun testKeycloakGetManagerToken (keycloak: KeycloakContainer) : String {
            val realmName = "SpringBootKeycloak"
            val clientId = "springboot-keycloak-client"
            val kcManager = KeycloakBuilder
                .builder()
                .serverUrl(keycloak.authServerUrl)
                .realm(realmName)
                .clientId(clientId)
                .username("manager@polito.it")
                .password("password")
                .build()
            kcManager.tokenManager().grantToken().expiresIn = 50000
            return kcManager.tokenManager().accessToken.token
        }

        fun testKeycloakGetClientToken (keycloak: KeycloakContainer) : String {
            val realmName = "SpringBootKeycloak"
            val clientId = "springboot-keycloak-client"
            val kcClient = KeycloakBuilder
                .builder()
                .serverUrl(keycloak.authServerUrl)
                .realm(realmName)
                .clientId(clientId)
                .username("client@polito.it")
                .password("password")
                .build()
            kcClient.tokenManager().grantToken().expiresIn = 50000
            return kcClient.tokenManager().accessToken.token
        }

        fun testKeycloakGetExpertToken (keycloak: KeycloakContainer) : String {
            val realmName = "SpringBootKeycloak"
            val clientId = "springboot-keycloak-client"
            val kcExpert = KeycloakBuilder
                .builder()
                .serverUrl(keycloak.authServerUrl)
                .realm(realmName)
                .clientId(clientId)
                .username("expert@polito.it")
                .password("password")
                .build()
            kcExpert.tokenManager().grantToken().expiresIn = 50000
            return kcExpert.tokenManager().accessToken.token
        }

        fun testKeycloakGetUser (keycloak: KeycloakContainer, email:String) : UserRepresentation? {
            val realmName = "SpringBootKeycloak"
            val clientId = "springboot-keycloak-client"

            return keycloak.keycloakAdminClient.realm(realmName).users().search(email)[0]
        }
    }
}