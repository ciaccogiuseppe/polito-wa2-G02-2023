package it.polito.wa2.server.ticketing.ticket

import it.polito.wa2.server.*
import it.polito.wa2.server.products.*
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileRole
import it.polito.wa2.server.profiles.ProfileService
import it.polito.wa2.server.ticketing.tickethistory.*
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.sql.Timestamp

@Service
class TicketServiceImpl(
    private val ticketRepository: TicketRepository,
    private val profileRepository: ProfileRepository,
    private val productRepository: ProductRepository,
    private val ticketHistoryRepository: TicketHistoryRepository,
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

    override fun addTicket(ticketDTO: TicketDTO): TicketIdDTO {
        // TODO: get user_id from session
        // productId is not null, already checked in the controller
        val product = getProduct(ticketDTO.productId)
        val customer = getProfileByEmail(ticketDTO.customerId!!)
        if (customer.role != ProfileRole.CUSTOMER)
            throw UnprocessableTicketException("The requester profile is not a customer")

        val ticket =  ticketRepository.save(ticketDTO.toNewTicket(product, customer))
        ticketHistoryRepository.save(
            newTicketHistory(
                ticket,
                ticket.customer!!,
                ticket.expert,
                TicketStatus.OPEN,
                ticket.status
            )
        )
        return TicketIdDTO(ticket.ticketId!!)
    }

    override fun assignTicket(ticketAssignDTO: TicketAssignDTO) {
        val expert = getProfileByEmail(ticketAssignDTO.expertId)
        val ticket = ticketRepository.findByIdOrNull(ticketAssignDTO.ticketId)
            ?: throw TicketNotFoundException("The ticket associated to the ID ${ticketAssignDTO.ticketId} does not exists")
        val oldState = ticket.status

        if (ticket.status != TicketStatus.OPEN && ticket.status == TicketStatus.REOPENED)
            throw UnprocessableTicketException("A ticket can't be assigned with the actual status")
        if (expert.role != ProfileRole.EXPERT)
            throw UnprocessableTicketException("The assigned profile is not an expert")

        ticket.expert = expert
        ticket.priority = ticketAssignDTO.priority
        ticket.status = TicketStatus.IN_PROGRESS

        ticketRepository.save(ticket)
        ticketHistoryRepository.save(
            newTicketHistory(
                ticket,
                ticket.customer!!,
                ticket.expert,
                oldState,
                ticket.status
            )
        )
    }

    override fun updateTicket(ticketUpdateDTO: TicketUpdateDTO) {
        val ticket = ticketRepository.findByIdOrNull(ticketUpdateDTO.ticketId)
            ?: throw TicketNotFoundException("The ticket associated to the ID ${ticketUpdateDTO.ticketId} does not exists")
        val oldState = ticket.status
        val newState = ticketUpdateDTO.newState

        when (ticket.status) {
            TicketStatus.OPEN -> {
                isNextStateValid(newState, hashSetOf(TicketStatus.RESOLVED, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS))
                if (newState == TicketStatus.IN_PROGRESS)
                    throw UnprocessableTicketException("It's not possible to set the status to <IN PROGRESS> without assigning an expert")
            }
            TicketStatus.RESOLVED -> isNextStateValid(newState, hashSetOf(TicketStatus.REOPENED, TicketStatus.CLOSED))
            TicketStatus.CLOSED -> isNextStateValid(newState, hashSetOf(TicketStatus.REOPENED))
            TicketStatus.IN_PROGRESS -> {
                isNextStateValid(newState, hashSetOf(TicketStatus.RESOLVED, TicketStatus.CLOSED, TicketStatus.OPEN))
                if (newState == TicketStatus.OPEN)
                    ticket.expert = null
            }
            TicketStatus.REOPENED -> {
                isNextStateValid(newState, hashSetOf(TicketStatus.RESOLVED, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS))
                if (newState == TicketStatus.IN_PROGRESS && ticket.expert == null)
                    throw UnprocessableTicketException("It's not possible to set the status to <IN PROGRESS> without assigning an expert")
            }
        }

        ticket.status = newState
        ticketRepository.save(ticket)
        ticketHistoryRepository.save(
            newTicketHistory(
                ticket,
                ticket.customer!!,
                ticket.expert,
                oldState,
                newState
            )
        )
    }

    private fun getProfile(profileId: Long): Profile {
        val profileDTO = profileService.getProfileById(profileId)
        return profileRepository.findByEmail(profileDTO.email)!!
    }

    private fun getProfileByEmail(email: String): Profile {
        val profileDTO = profileService.getProfile(email)
        return profileRepository.findByEmail(profileDTO.email)!!
    }

    private fun getProduct(productId: String): Product {
        val productDTO = productService.getProduct(productId)
        return productRepository.findByIdOrNull(productDTO.productId)!!
    }

    private fun isNextStateValid(newStatus: TicketStatus, validValues: HashSet<TicketStatus>) {
        if (!validValues.contains(newStatus))
            throw UnprocessableTicketException("The new state is invalid according to the current state")
    }
}
