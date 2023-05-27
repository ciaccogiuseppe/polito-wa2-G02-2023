package it.polito.wa2.server

import it.polito.wa2.server.products.Product
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileRole
import it.polito.wa2.server.ticketing.attachment.Attachment
import it.polito.wa2.server.ticketing.message.Message
import it.polito.wa2.server.ticketing.ticket.Ticket
import it.polito.wa2.server.ticketing.ticket.TicketStatus
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

        fun testProduct(productId: String, name:String, brand:String) : Product {
            val product = Product()
            product.productId = productId
            product.name = name
            product.brand = brand

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

        fun <T> testEntityHeader(body: T?, token: String): HttpEntity<T> {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers.setBearerAuth(token)

            return HttpEntity(body, headers)
        }
    }
}