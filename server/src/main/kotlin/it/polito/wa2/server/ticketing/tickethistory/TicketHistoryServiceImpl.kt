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

@Service @Transactional
class TicketHistoryServiceImpl(
    private val ticketHistoryRepository: TicketHistoryRepository,
    private val ticketService: TicketService,
    private val ticketRepository: TicketRepository,
    private val profileRepository: ProfileRepository,
    private val profileService: ProfileService
): TicketHistoryService {
    @Transactional(readOnly = true)
    override fun getAllTicketHistory(): List<TicketHistoryDTO> {
        return ticketHistoryRepository.findAll().map {it.toDTO()}
    }

    @Transactional(readOnly = true)
    override fun getTicketHistory(ticketId: Long): List<TicketHistoryDTO> {
        val ticket = getTicket(ticketId)
        return ticketHistoryRepository.findAllByTicket(ticket).map {it.toDTO()}
    }

    override fun addTicketHistory(ticketHistoryDTO: TicketHistoryDTO) {
        val ticket = getTicket(ticketHistoryDTO.ticketId)
        val user = getProfile(ticketHistoryDTO.userId)
        val currentExpert = if(ticketHistoryDTO.currentExpertId != null)
            getProfile(ticketHistoryDTO.currentExpertId)
        else
            null
        ticketHistoryRepository.save(ticketHistoryDTO
            .toNewTicketHistory(ticket, user, currentExpert))
    }

    private fun getTicket(ticketId: Long): Ticket {
        val ticketDTO = ticketService.getTicket(ticketId)
        return ticketRepository.findByIdOrNull(ticketDTO.ticketId)!!
    }

    private fun getProfile(profileId: String): Profile {
        val profileDTO = profileService.getProfile(profileId)
        return profileRepository.findByEmail(profileDTO.email)!!
    }

}