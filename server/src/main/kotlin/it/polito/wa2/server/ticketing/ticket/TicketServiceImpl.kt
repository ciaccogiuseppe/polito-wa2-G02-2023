package it.polito.wa2.server.ticketing.ticket

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.*
import it.polito.wa2.server.items.Item
import it.polito.wa2.server.items.ItemRepository
import it.polito.wa2.server.items.ItemService
import it.polito.wa2.server.products.*
import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileRole
import it.polito.wa2.server.profiles.ProfileService
import it.polito.wa2.server.security.WebSecurityConfig
import it.polito.wa2.server.ticketing.tickethistory.*
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.LocalDateTime

@Service
@Transactional
@Observed
class TicketServiceImpl(
    private val ticketRepository: TicketRepository,
    private val profileRepository: ProfileRepository,
    private val productRepository: ProductRepository,
    private val ticketHistoryRepository: TicketHistoryRepository,
    private val productService: ProductService,
    private val profileService: ProfileService,
    private val itemRepository: ItemRepository,
    private val itemService: ItemService
) : TicketService {
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('${WebSecurityConfig.MANAGER}')")
    override fun managerGetTicket(ticketId: Long, managerEmail: String): TicketDTO {
        getProfileByEmail(managerEmail, managerEmail)
        val ticket = getTicket(ticketId)
        return ticket.toDTO()
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('${WebSecurityConfig.CLIENT}')")
    override fun clientGetTicket(ticketId: Long, clientEmail: String): TicketDTO {
        val ticket = getTicket(ticketId)
        if (clientEmail != ticket.client!!.email)
            throw ForbiddenException("The ticket is associated to a different client")
        return ticket.toDTO()
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('${WebSecurityConfig.EXPERT}')")
    override fun expertGetTicket(ticketId: Long, expertEmail: String): TicketDTO {
        val ticket = getTicket(ticketId)
        if (expertEmail != ticket.expert!!.email)
            throw ForbiddenException("The ticket is assigned to a different expert")
        return ticket.toDTO()
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('${WebSecurityConfig.CLIENT}')")
    override fun clientGetTicketsFiltered(
        clientEmail: String?,
        minPriority: Int?,
        maxPriority: Int?,
        productId: String?,
        createdAfter: Timestamp?,
        createdBefore: Timestamp?,
        expertEmail: String?,
        status: List<TicketStatus>?,
        userEmail: String
    ): List<TicketDTO> {
        val client: Profile = getProfileByEmail(userEmail, userEmail)
        val expert = if (expertEmail != null) getProfileByEmail(expertEmail, userEmail) else null
        val product = if (productId != null) getProduct(productId) else null
        return if (clientEmail != client.email) listOf()
        else filterTickets(client, minPriority, maxPriority, product, createdAfter, createdBefore, expert, status)
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('${WebSecurityConfig.EXPERT}')")
    override fun expertGetTicketsFiltered(
        clientEmail: String?,
        minPriority: Int?,
        maxPriority: Int?,
        productId: String?,
        createdAfter: Timestamp?,
        createdBefore: Timestamp?,
        expertEmail: String?,
        status: List<TicketStatus>?,
        userEmail: String
    ): List<TicketDTO> {
        val client = if (clientEmail != null) getProfileByEmail(clientEmail, userEmail) else null
        val expert: Profile = getProfileByEmail(userEmail, userEmail)
        val product: Product? = if (productId != null) getProduct(productId) else null

        return if (expertEmail != expert.email) listOf()
        else filterTickets(client, minPriority, maxPriority, product, createdAfter, createdBefore, expert, status)
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('${WebSecurityConfig.MANAGER}')")
    override fun managerGetTicketsFiltered(
        clientEmail: String?,
        minPriority: Int?,
        maxPriority: Int?,
        productId: String?,
        createdAfter: Timestamp?,
        createdBefore: Timestamp?,
        expertEmail: String?,
        status: List<TicketStatus>?,
        userEmail: String
    ): List<TicketDTO> {
        val client = if (clientEmail != null) getProfileByEmail(clientEmail, userEmail) else null
        val expert = if (expertEmail != null) getProfileByEmail(expertEmail, userEmail) else null
        val product = if (productId != null) getProduct(productId) else null

        return filterTickets(client, minPriority, maxPriority, product, createdAfter, createdBefore, expert, status)
    }

    @PreAuthorize("hasRole('${WebSecurityConfig.CLIENT}')")
    override fun addTicket(ticketDTO: TicketDTO, userEmail: String): TicketIdDTO {
        val timestamp = Timestamp.valueOf(LocalDateTime.now())
        val item = getItem(ticketDTO.productId, ticketDTO.serialNum, userEmail)
        val client = getProfileByEmail(userEmail, userEmail)
        if (item.client != client)
            throw ForbiddenException("You cannot create a ticket for this item")
        checkWarrantyValidity(item, timestamp)
        val ticket = ticketRepository.save(ticketDTO.toNewTicket(item, client, timestamp))

        ticketHistoryRepository.save(
            newTicketHistory(
                ticket,
                ticket.client!!,
                ticket.expert,
                TicketStatus.OPEN,
                ticket.status
            )
        )
        return TicketIdDTO(ticket.getId()!!)
    }

    @PreAuthorize("hasRole('${WebSecurityConfig.MANAGER}')")
    override fun assignTicket(ticketAssignDTO: TicketAssignDTO, userEmail: String) {
        val expert = getProfileByEmail(ticketAssignDTO.expertEmail, userEmail)
        val ticket = getTicket(ticketAssignDTO.ticketId)
        val oldState = ticket.status

        if (ticket.status != TicketStatus.OPEN && ticket.status != TicketStatus.REOPENED)
            throw UnprocessableTicketException("A ticket can't be assigned with the actual status")
        if (expert.role != ProfileRole.EXPERT)
            throw UnprocessableTicketException("The assigned profile is not an expert")

        val user = getProfileByEmail(userEmail, userEmail)

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

    @PreAuthorize("hasRole('${WebSecurityConfig.MANAGER}')")
    override fun managerUpdateTicket(ticketUpdateDTO: TicketUpdateDTO, userEmail: String) {
        val ticket = getTicket(ticketUpdateDTO.ticketId)
        val user = getProfileByEmail(userEmail, userEmail)

        updateTicket(ticketUpdateDTO, ticket, user)
    }

    @PreAuthorize("hasRole('${WebSecurityConfig.CLIENT}')")
    override fun clientUpdateTicket(ticketUpdateDTO: TicketUpdateDTO, userEmail: String) {
        val ticket = getTicket(ticketUpdateDTO.ticketId)
        val user = getProfileByEmail(userEmail, userEmail)
        if (user != ticket.client)
            throw ForbiddenException("It's not possible to set the status of tickets that are not yours")

        updateTicket(ticketUpdateDTO, ticket, user)
    }

    @PreAuthorize("hasRole('${WebSecurityConfig.EXPERT}')")
    override fun expertUpdateTicket(ticketUpdateDTO: TicketUpdateDTO, userEmail: String) {
        val ticket = getTicket(ticketUpdateDTO.ticketId)
        val user = getProfileByEmail(userEmail, userEmail)
        if (user != ticket.expert)
            throw ForbiddenException("It's not possible to set the status of tickets that are not assigned to you")
        updateTicket(ticketUpdateDTO, ticket, user)
    }

    private fun filterTickets(
        client: Profile?,
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
                (client == null || it.client == client) &&
                        (minPriority == null || it.priority >= minPriority) &&
                        (maxPriority == null || it.priority <= maxPriority) &&
                        (product == null || it.item!!.product == product) &&
                        (createdAfter == null || it.createdTimestamp!!.after(createdAfter) || it.createdTimestamp!!.equals(
                            createdAfter
                        )) &&
                        (createdBefore == null || it.createdTimestamp!!.before(createdBefore) || it.createdTimestamp!!.equals(
                            createdBefore
                        )) &&
                        (expert == null || it.expert == expert) &&
                        (status == null || status.contains(it.status))
            }.map { it.toDTO() }
    }

    private fun updateTicket(ticketUpdateDTO: TicketUpdateDTO, ticket: Ticket, user: Profile) {
        val oldState = ticket.status
        val newState = ticketUpdateDTO.newState

        when (user.role) {
            ProfileRole.MANAGER -> checkNewStateManager(ticketUpdateDTO.newState, ticket)
            ProfileRole.EXPERT -> checkNewStateExpert(ticketUpdateDTO.newState, ticket)
            ProfileRole.CLIENT -> checkNewStateClient(ticketUpdateDTO.newState, ticket)
            else -> {}
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

    private fun checkNewStateClient(newState: TicketStatus, ticket: Ticket) {
        when (ticket.status) {
            TicketStatus.OPEN -> isNextStateValid(newState, hashSetOf(TicketStatus.RESOLVED))
            TicketStatus.RESOLVED -> {
                isNextStateValid(newState, hashSetOf(TicketStatus.REOPENED))
                checkWarrantyValidity(ticket.item!!, Timestamp.valueOf(LocalDateTime.now()))
            }

            TicketStatus.CLOSED -> {
                isNextStateValid(newState, hashSetOf(TicketStatus.REOPENED))
                checkWarrantyValidity(ticket.item!!, Timestamp.valueOf(LocalDateTime.now()))
            }

            TicketStatus.IN_PROGRESS -> isNextStateValid(newState, hashSetOf(TicketStatus.RESOLVED))
            TicketStatus.REOPENED -> isNextStateValid(newState, hashSetOf(TicketStatus.RESOLVED))
        }
        if (newState == TicketStatus.REOPENED)
            ticket.expert = null
    }

    private fun checkNewStateExpert(newState: TicketStatus, ticket: Ticket) {
        when (ticket.status) {
            TicketStatus.OPEN -> isNextStateValid(newState, hashSetOf(TicketStatus.CLOSED))
            TicketStatus.RESOLVED -> isNextStateValid(newState, hashSetOf(TicketStatus.CLOSED))
            TicketStatus.CLOSED -> isNextStateValid(newState, hashSetOf())
            TicketStatus.IN_PROGRESS -> isNextStateValid(newState, hashSetOf(TicketStatus.CLOSED))
            TicketStatus.REOPENED -> isNextStateValid(newState, hashSetOf(TicketStatus.CLOSED))
        }
    }

    private fun checkNewStateManager(newState: TicketStatus, ticket: Ticket) {
        when (ticket.status) {
            TicketStatus.OPEN -> {
                isNextStateValid(
                    newState,
                    hashSetOf(TicketStatus.RESOLVED, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS)
                )
                if (newState == TicketStatus.IN_PROGRESS)
                    throw UnprocessableTicketException("It's not possible to set the status to <IN PROGRESS>: the expert must be assigned")
            }

            TicketStatus.RESOLVED -> isNextStateValid(newState, hashSetOf(TicketStatus.CLOSED))
            TicketStatus.CLOSED -> isNextStateValid(newState, hashSetOf())
            TicketStatus.IN_PROGRESS -> {
                isNextStateValid(newState, hashSetOf(TicketStatus.RESOLVED, TicketStatus.CLOSED, TicketStatus.OPEN))
                if (newState == TicketStatus.OPEN) {
                    ticket.expert = null
                }
            }

            TicketStatus.REOPENED -> {
                isNextStateValid(
                    newState,
                    hashSetOf(TicketStatus.RESOLVED, TicketStatus.CLOSED, TicketStatus.IN_PROGRESS)
                )
                if (newState == TicketStatus.IN_PROGRESS && ticket.expert == null)
                    throw UnprocessableTicketException("It's not possible to set the status to <IN PROGRESS>: the expert must be assigned")
            }
        }
    }

    private fun getProfileByEmail(email: String, loggedEmail: String): Profile {
        val profileDTO = profileService.getProfile(email, loggedEmail)
        return profileRepository.findByEmail(profileDTO.email)!!
    }

    private fun getProduct(productId: String): Product {
        val productDTO = productService.getProduct(productId)
        return productRepository.findByIdOrNull(productDTO.productId)!!
    }

    private fun getItem(productId: String, serialNum: Long, email: String): Item {
        val itemDTO = itemService.getItemClient(productId, serialNum, email)
        val product = getProduct(itemDTO.productId)
        return itemRepository.findByProductAndSerialNum(product, itemDTO.serialNum)!!
    }

    private fun getTicket(ticketId: Long): Ticket {
        return ticketRepository.findByIdOrNull(ticketId)
            ?: throw TicketNotFoundException("The ticket associated to the ID $ticketId does not exists")
    }

    private fun isNextStateValid(newStatus: TicketStatus, validValues: HashSet<TicketStatus>) {
        if (!validValues.contains(newStatus))
            throw UnprocessableTicketException("The new state is invalid according to the current state and the user role")
    }

    private fun checkWarrantyValidity(item: Item, timestamp: Timestamp) {
        if (item.validFromTimestamp!!.toLocalDateTime().plusMonths(item.durationMonths!!)
                .isBefore(timestamp.toLocalDateTime())
        )
            throw ForbiddenException("Warranty has expired for this item")
    }
}
