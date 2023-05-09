package it.polito.wa2.server.ticketing.ticket

import it.polito.wa2.server.*
import it.polito.wa2.server.products.*
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.sql.Timestamp

@Service
class TicketServiceImpl(
    private val ticketRepository: TicketRepository,
    private val productService: ProductService,
    private val profileService: ProfileService,
    private val profileRepository: ProfileRepository,
    private val productRepository: ProductRepository
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
        status: List<TicketStatus>?
    ): List<TicketDTO> {
        var customer: Profile? = null
        var expert: Profile? = null
        var product: Product? = null

        if (customerId != null)
            customer = getProfile(customerId)
        if (expertId != null)
            expert = getProfile(expertId)
        if (productId != null)
            product = getProduct(productId)

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

    override fun addTicket(ticket: TicketDTO): TicketIdDTO {
        // TODO: get user_id from session
        // productId is not null, already checked in the controller
        val product = getProduct(ticket.productId)
        val customer = getProfile(1)
        val ticketId =  ticketRepository.save(ticket.toNewTicket(product, customer)).ticketId!!
        return TicketIdDTO(ticketId)
    }

    private fun getProfile(profileId: Long): Profile {
        val profileDTO = profileService.getProfileById(profileId)
        return profileRepository.findByEmail(profileDTO.email)!!
    }

    private fun getProduct(productId: String): Product {
        val productDTO = productService.getProduct(productId)
        return productRepository.findByIdOrNull(productDTO.productId)!!
    }

}
