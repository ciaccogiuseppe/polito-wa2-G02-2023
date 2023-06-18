package it.polito.wa2.server.ticketing.attachment


import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.AttachmentNotFoundException
import it.polito.wa2.server.ForbiddenException
import it.polito.wa2.server.profiles.ProfileRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service @Transactional @Observed
class AttachmentServiceImpl(
    private val attachmentRepository: AttachmentRepository, private val profileRepository: ProfileRepository
): AttachmentService {
    @Transactional(readOnly = true)
    override fun getAttachment(attachmentID: Long, userEmail: String): AttachmentDTO {
        val attachment = attachmentRepository.findByIdOrNull(attachmentID)?:
            throw AttachmentNotFoundException("Attachment with id '${attachmentID}' not found")
        val user = profileRepository.findByEmail(userEmail)?:
            throw ForbiddenException("It's not possible to get an attachment if user is not registered")
        val ticket = attachment.message?.ticket
        val customerOfAttachment = ticket?.customer
        val expertOfAttachment = ticket?.expert
        if(user != customerOfAttachment && user != expertOfAttachment)
            throw ForbiddenException("It's not possible to get an attachment of a message belonging to a ticket in which you are not participating")
        return attachment.toDTO()
    }

    @Transactional(readOnly = true)
    override fun getAttachmentManager(attachmentID: Long): AttachmentDTO {
        val attachment = attachmentRepository.findByIdOrNull(attachmentID)?:
            throw AttachmentNotFoundException("Attachment with id '${attachmentID}' not found")
        return attachment.toDTO()
    }

    override fun addAttachment(attachmentDTO: AttachmentDTO): Long {
        return attachmentRepository.save(attachmentDTO.toNewAttachment()).getId()!!
    }
}