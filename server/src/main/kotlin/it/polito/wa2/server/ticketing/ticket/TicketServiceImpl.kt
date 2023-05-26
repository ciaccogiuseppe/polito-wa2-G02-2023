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
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp

@Service @Transactional
class TicketServiceImpl(
    private val ticketRepository: TicketRepository,
    private val profileRepository: ProfileRepository,
    private val productRepository: ProductRepository,
    private val ticketHistoryRepository: TicketHistoryRepository,
    private val productService: ProductService,
    private val profileService: ProfileService
): TicketService {
    @Transactional(readOnly = true)
    override fun managerGetTicket(ticketId: Long, managerEmail: String): TicketDTO {
        getProfileByEmail(managerEmail)
        val ticket = ticketRepository.findByIdOrNull(ticketId)
            ?: throw TicketNotFoundException("Ticket with id '${ticketId}' not found")
        return ticket.toDTO()
    }

    @Transactional(readOnly = true)
    override fun clientGetTicket(ticketId: Long, clientEmail: String): TicketDTO {
        val ticket = ticketRepository.findByIdOrNull(ticketId)
            ?: throw TicketNotFoundException("Ticket with id '${ticketId}' not found")
        if (clientEmail != ticket.customer!!.email)
            throw ForbiddenException("The ticket is associated to a different client")
        return ticket.toDTO()
    }

    @Transactional(readOnly = true)
    override fun expertGetTicket(ticketId: Long, expertEmail: String): TicketDTO {
        val ticket = ticketRepository.findByIdOrNull(ticketId)
            ?: throw TicketNotFoundException("Ticket with id '${ticketId}' not found")
        if (expertEmail != ticket.expert!!.email)
            throw ForbiddenException("The ticket is assigned to a different expert")
        return ticket.toDTO()
    }

    @Transactional(readOnly = true)
    override fun clientGetTicketsFiltered(
        customerEmail: String?,
        minPriority: Int?,
        maxPriority: Int?,
        productId: String?,
        createdAfter: Timestamp?,
        createdBefore: Timestamp?,
        expertEmail: String?,
        status: List<TicketStatus>?,
        userEmail: String
    ): List<TicketDTO> {
        val customer: Profile = getProfileByEmail(userEmail)
        val expert = if (expertEmail != null) getProfileByEmail(expertEmail) else null
        val product = if (productId != null) getProduct(productId) else null
        return if (customerEmail != customer.email) listOf()
            else filterTickets(customer, minPriority, maxPriority, product, createdAfter, createdBefore, expert, status)
    }

    @Transactional(readOnly = true)
    override fun expertGetTicketsFiltered(
        customerEmail: String?,
        minPriority: Int?,
        maxPriority: Int?,
        productId: String?,
        createdAfter: Timestamp?,
        createdBefore: Timestamp?,
        expertEmail: String?,
        status: List<TicketStatus>?,
        userEmail: String
    ): List<TicketDTO> {
        val customer = if (customerEmail != null) getProfileByEmail(customerEmail) else null
        val expert: Profile = getProfileByEmail(userEmail)
        val product: Product? = if (productId != null) getProduct(productId) else null

        return if (expertEmail != expert.email) listOf()
            else filterTickets(customer, minPriority, maxPriority, product, createdAfter, createdBefore, expert, status)
    }

    @Transactional(readOnly = true)
    override fun managerGetTicketsFiltered(
        customerEmail: String?,
        minPriority: Int?,
        maxPriority: Int?,
        productId: String?,
        createdAfter: Timestamp?,
        createdBefore: Timestamp?,
        expertEmail: String?,
        status: List<TicketStatus>?,
        userEmail: String
    ): List<TicketDTO> {
        val customer = if (customerEmail != null) getProfileByEmail(customerEmail) else null
        val expert = if (expertEmail != null) getProfileByEmail(expertEmail) else null
        val product = if (productId != null) getProduct(productId) else null

        return filterTickets(customer, minPriority, maxPriority, product, createdAfter, createdBefore, expert, status)
    }

    override fun addTicket(ticketDTO: TicketDTO, userEmail: String): TicketIdDTO {
        val product = getProduct(ticketDTO.productId)
        val customer = getProfileByEmail(userEmail)
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
        return TicketIdDTO(ticket.getId()!!)
    }

    override fun assignTicket(ticketAssignDTO: TicketAssignDTO, userEmail: String) {
        val expert = getProfileByEmail(ticketAssignDTO.expertEmail)
        val ticket = ticketRepository.findByIdOrNull(ticketAssignDTO.ticketId)
            ?: throw TicketNotFoundException("The ticket associated to the ID ${ticketAssignDTO.ticketId} does not exists")
        val oldState = ticket.status

        if (ticket.status != TicketStatus.OPEN && ticket.status != TicketStatus.REOPENED)
            throw UnprocessableTicketException("A ticket can't be assigned with the actual status")
        if (expert.role != ProfileRole.EXPERT)
            throw UnprocessableTicketException("The assigned profile is not an expert")

        val user = getProfileByEmail(userEmail)

        ticket.expert = expert
        ticket.priority = ticketAssignDTO.priority
        ticket.status = TicketStatus.IN_PROGRESS

        ticketRepository.save(ticket)
        ticketHistoryRepository.save(
            newTicketHistory(
                ticket,
                user,
                ticket.expert,
                oldState,
                ticket.status
            )
        )
    }

    override fun managerUpdateTicket(ticketUpdateDTO: TicketUpdateDTO, userEmail: String) {
        val ticket = ticketRepository.findByIdOrNull(ticketUpdateDTO.ticketId)
            ?: throw TicketNotFoundException("The ticket associated to the ID ${ticketUpdateDTO.ticketId} does not exists")
        val user = getProfileByEmail(userEmail)

        updateTicket(ticketUpdateDTO, ticket, user, true)
    }

    override fun clientUpdateTicket(ticketUpdateDTO: TicketUpdateDTO, userEmail: String) {
        val ticket = ticketRepository.findByIdOrNull(ticketUpdateDTO.ticketId)
            ?: throw TicketNotFoundException("The ticket associated to the ID ${ticketUpdateDTO.ticketId} does not exists")
        val user = getProfileByEmail(userEmail)
        if (user != ticket.customer)
            throw ForbiddenException("It's not possible to set the status of tickets that are not yours")

        updateTicket(ticketUpdateDTO, ticket, user, false)
    }

    override fun expertUpdateTicket(ticketUpdateDTO: TicketUpdateDTO, userEmail: String) {
        val ticket = ticketRepository.findByIdOrNull(ticketUpdateDTO.ticketId)
            ?: throw TicketNotFoundException("The ticket associated to the ID ${ticketUpdateDTO.ticketId} does not exists")
        val user = getProfileByEmail(userEmail)
        if (user != ticket.expert)
            throw ForbiddenException("It's not possible to set the status of tickets that are not assigned to you")

        updateTicket(ticketUpdateDTO, ticket, user, false)
    }

    private fun filterTickets(
        customer: Profile?,
        minPriority: Int?,
        maxPriority: Int?,
        product: Product?,
        createdAfter: Timestamp?,
        createdBefore: Timestamp?,
        expert: Profile?,
        status: List<TicketStatus>?
    ): List<TicketDTO> {
        return ticketRepository
            .findAll()
            .filter {
                (customer == null || it.customer == customer) &&
                (minPriority == null || it.priority >= minPriority) &&
                (maxPriority == null || it.priority <= maxPriority) &&
                (product == null || it.product == product) &&
                (createdAfter == null || it.createdTimestamp!!.after(createdAfter) || it.createdTimestamp!!.equals(createdAfter)) &&
                (createdBefore == null || it.createdTimestamp!!.before(createdBefore) || it.createdTimestamp!!.equals(createdBefore)) &&
                (expert == null || it.expert == expert) &&
                (status == null || status.contains(it.status))
            }.map { it.toDTO() }
    }

    private fun updateTicket(ticketUpdateDTO: TicketUpdateDTO, ticket: Ticket, user: Profile, isManager: Boolean) {
        val oldState = ticket.status
        val newState = ticketUpdateDTO.newState

        when (ticket.status) {
            TicketStatus.OPEN -> {
                isNextStateValid(newState, hashSetOf(TicketStatus.RESOLVED, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS))
                if (newState == TicketStatus.IN_PROGRESS)
                    throw UnprocessableTicketException("It's not possible to set the status to <IN PROGRESS>: the expert must be assigned")
            }
            TicketStatus.RESOLVED -> isNextStateValid(newState, hashSetOf(TicketStatus.REOPENED, TicketStatus.CLOSED))
            TicketStatus.CLOSED -> isNextStateValid(newState, hashSetOf(TicketStatus.REOPENED))
            TicketStatus.IN_PROGRESS -> {
                isNextStateValid(newState, hashSetOf(TicketStatus.RESOLVED, TicketStatus.CLOSED, TicketStatus.OPEN))
                if (newState == TicketStatus.OPEN) {
                    if (isManager) ticket.expert = null
                    else throw ForbiddenException("Only the manager can set the status to <OPEN>")
                }
            }
            TicketStatus.REOPENED -> {
                isNextStateValid(newState, hashSetOf(TicketStatus.RESOLVED, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS))
                if (newState == TicketStatus.IN_PROGRESS)
                    throw UnprocessableTicketException("It's not possible to set the status to <IN PROGRESS>: the expert must be assigned")
            }
        }

        ticket.status = newState
        ticketRepository.save(ticket)
        ticketHistoryRepository.save(
            newTicketHistory(
                ticket,
                user,
                ticket.expert,
                oldState,
                newState
            )
        )
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
