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
    override fun getTicket(ticket_id: Long): TicketDTO {
        val ticket = ticketRepository.findByIdOrNull(ticket_id)
            ?: throw TicketNotFoundException("Ticket with id '${ticket_id}' not found")
        return ticket.toDTO()
    }

    override fun getTicketsFiltered(
        customer_id: Long?,
        min_priority: Int?,
        max_priority: Int?,
        product_id: String?,
        created_after: Timestamp?,
        created_before: Timestamp?,
        expert_id: Long?,
        status: List<String>?
    ): List<TicketDTO> {
        var customer: Profile? = null
        var expert: Profile? = null
        var product: Product? = null

        if (customer_id != null)
            customer = profileService.getProfileById(customer_id).toProfile()
        if (expert_id != null)
            expert = profileService.getProfileById(expert_id).toProfile()
        if (product_id != null)
            product = productService.getProduct(product_id).toProduct()

        return ticketRepository
            .findAll()
            .filter {
                (customer != null && it.customer_id == customer) &&
                (min_priority != null && it.priority >= min_priority) &&
                (max_priority != null && it.priority <= max_priority) &&
                (product != null && it.product_id == product) &&
                (created_after != null && it.timestamp!!.after(created_after)) &&
                (created_before != null && it.timestamp!!.before(created_before)) &&
                (expert != null && it.expert_id == expert) &&
                (status != null && status.contains(it.status))
            }.map { it.toDTO() }
    }

    override fun addTicket(ticket: TicketDTO): Long {
        // TODO: get user_id from session
        val id: Long = 1
        // productId is not null, already checked in the controller
        checkIfProductAndCustomerExists(ticket.productId!!, id)
        return ticketRepository.save(ticket.toTicket()).ticketId!!
    }

    private fun checkIfProductAndCustomerExists(prod: Product, customer_id: Long) {
        productService.getProduct(prod.productId)
        profileService.getProfileById(customer_id)
    }
}