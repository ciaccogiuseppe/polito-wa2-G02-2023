package it.polito.wa2.server.ticketing.ticket

import it.polito.wa2.server.*
import it.polito.wa2.server.products.Product
import it.polito.wa2.server.products.ProductService
import it.polito.wa2.server.products.toProduct
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileService
import it.polito.wa2.server.profiles.toProfile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.sql.Timestamp

@Service
class TicketServiceImpl(
    private val ticketRepository: TicketRepository,
    private val productService: ProductService,
    private val profileService: ProfileService
): TicketService {
    override fun getTicket(ticketId: Long): TicketDTO {
        val ticket = ticketRepository.findByIdOrNull(ticketId)
            ?: throw TicketNotFoundException("Ticket with id '${ticketId}' not found")
        return ticket.toDTO()
    }

    override fun getTicketsFiltered(
        customerId: Long?,
        minPriority: Int?,
        maxPriority: Int?,
        productId: String?,
        createdAfter: Timestamp?,
        createdBefore: Timestamp?,
        expertId: Long?,
        status: List<String>?
    ): List<TicketDTO> {
        var customer: Profile? = null
        var expert: Profile? = null
        var product: Product? = null

        if (customerId != null)
            customer = profileService.getProfileById(customerId).toProfile()
        if (expertId != null)
            expert = profileService.getProfileById(expertId).toProfile()
        if (productId != null)
            product = productService.getProduct(productId).toProduct()

        return ticketRepository
            .findAll()
            .filter {
                (customer != null && it.customer == customer) &&
                (minPriority != null && it.priority >= minPriority) &&
                (maxPriority != null && it.priority <= maxPriority) &&
                (product != null && it.product == product) &&
                (createdAfter != null && it.createdTimestamp!!.after(createdAfter)) &&
                (createdBefore != null && it.createdTimestamp!!.before(createdBefore)) &&
                (expert != null && it.expert == expert) &&
                (status != null && status.contains(it.status))
            }.map { it.toDTO() }
    }

    override fun addTicket(ticket: TicketDTO): Long {
        // TODO: get user_id from session
        val id: Long = 1
        // productId is not null, already checked in the controller
        checkIfProductAndCustomerExists(ticket.product!!, id)
        return ticketRepository.save(ticket.toTicket()).ticketId!!
    }

    private fun checkIfProductAndCustomerExists(prod: Product, customerId: Long) {
        productService.getProduct(prod.productId)
        profileService.getProfileById(customerId)
    }
}