package it.polito.wa2.server.ticketing.ticket

import it.polito.wa2.server.*
import it.polito.wa2.server.products.*
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileService
//import it.polito.wa2.server.profiles.toProfile
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

    /*
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
            customer = profileService.getProfileById(customerId).toProfile(profileRepository)
        if (expertId != null)
            expert = profileService.getProfileById(expertId).toProfile(profileRepository)
        if (productId != null)
            product = productService.getProduct(productId).toProduct(productRepository)

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
     */

    override fun addTicket(ticket: TicketDTO): TicketIdDTO {
        // TODO: get user_id from session
        val userId: Long = 1
        // productId is not null, already checked in the controller
        val returnedEntities = checkIfProductAndCustomerExists(ticket.productId, userId)
        val ticketId =  ticketRepository.save(ticket.toNewTicket(returnedEntities.product, returnedEntities.customer)).ticketId!!
        return TicketIdDTO(ticketId)
    }

    private fun checkIfProductAndCustomerExists(productId: String, customerId: Long): RepositoryObjectsTicketServ {
        val repProductId = productService.getProduct(productId).productId
        val repProfileId = profileService.getProfileById(customerId).email
        val product = productRepository.findByIdOrNull(repProductId)!!
        val customer = profileRepository.findByEmail(repProfileId)!!
        return RepositoryObjectsTicketServ(product, customer)
    }

    data class RepositoryObjectsTicketServ(val product: Product, val customer:Profile)
}
