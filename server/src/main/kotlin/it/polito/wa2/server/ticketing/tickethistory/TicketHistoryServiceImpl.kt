package it.polito.wa2.server.ticketing.tickethistory

import it.polito.wa2.server.profiles.Profile
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileService
import it.polito.wa2.server.ticketing.ticket.Ticket
import it.polito.wa2.server.ticketing.ticket.TicketRepository
import it.polito.wa2.server.ticketing.ticket.TicketService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp

@Service @Transactional
class TicketHistoryServiceImpl(
    private val ticketHistoryRepository: TicketHistoryRepository,
    private val ticketService: TicketService,
    private val ticketRepository: TicketRepository,
    private val profileService: ProfileService,
    private val profileRepository: ProfileRepository
): TicketHistoryService {
    @Transactional(readOnly = true)
    override fun getAllTicketHistory(): List<TicketHistoryDTO> {
        return ticketHistoryRepository.findAll().map {it.toDTO()}
    }

    @Transactional(readOnly = true)
    override fun getTicketHistoryFiltered(
        ticketId: Long?,
        userId: Long?,
        updatedAfter: Timestamp?,
        updatedBefore: Timestamp?,
        currentExpertId: Long?,
        userEmail: String
    ): List<TicketHistoryDTO> {
        var user: Profile? = null
        var currentExpert: Profile? = null
        var ticket: Ticket? = null

        if (ticketId != null)
            ticket = getTicket(ticketId, userEmail)
        if (userId != null)
            user = getProfile(userId)
        if (currentExpertId != null)
            currentExpert = getProfile(currentExpertId)
        return ticketHistoryRepository
            .findAll()
            .filter {
                (ticket == null || it.ticket == ticket) &&
                        (user == null || it.user == user) &&
                        (updatedAfter == null || it.updatedTimestamp!!.after(updatedAfter) || it.updatedTimestamp!!.equals(updatedAfter)) &&
                        (updatedBefore == null || it.updatedTimestamp!!.before(updatedBefore) || it.updatedTimestamp!!.equals(updatedBefore)) &&
                        (currentExpert == null || it.currentExpert == currentExpert)
            }.map { it.toDTO() }
    }

    private fun getTicket(ticketId: Long, userEmail: String): Ticket {
        val ticketDTO = ticketService.managerGetTicket(ticketId, userEmail)
        return ticketRepository.findByIdOrNull(ticketDTO.ticketId)!!
    }

    private fun getProfile(profileId: Long): Profile {
        val profileDTO = profileService.getProfileById(profileId)
        return profileRepository.findByEmail(profileDTO.email)!!
    }
}