package it.polito.wa2.server.ticketing.attachment


import it.polito.wa2.server.AttachmentNotFoundException
import it.polito.wa2.server.ForbiddenException
import it.polito.wa2.server.profiles.ProfileRepository
import it.polito.wa2.server.profiles.ProfileRole
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service @Transactional
class AttachmentServiceImpl(
    private val attachmentRepository: AttachmentRepository, private val profileRepository: ProfileRepository
): AttachmentService {
    @Transactional(readOnly = true)
    override fun getAttachment(attachmentID: Long, userEmail: String): AttachmentDTO {
        val attachment = attachmentRepository.findByIdOrNull(attachmentID)?:
            throw AttachmentNotFoundException("Attachment with id '${attachmentID}' not found")
        val user = profileRepository.findByEmail(userEmail)?:
            throw ForbiddenException("It's not possible to get an attachment if user is not registered")
        val ticket = attachment.message!!.ticket!!
        val customerOfAttachment = ticket.customer!!
        val expertOfAttachment = ticket.expert!!
        if(user != customerOfAttachment && user != expertOfAttachment && user.role != ProfileRole.MANAGER)
            throw ForbiddenException("It's not possible to get an attachment of a message belonging to a ticket in which you are not participating")
        return attachment.toDTO()
    }

    override fun addAttachment(attachmentDTO: AttachmentDTO): Long {
        return attachmentRepository.save(attachmentDTO.toNewAttachment()).attachmentId!!
    }
}