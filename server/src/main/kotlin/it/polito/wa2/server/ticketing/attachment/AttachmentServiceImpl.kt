package it.polito.wa2.server.ticketing.attachment


import it.polito.wa2.server.AttachmentNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service @Transactional
class AttachmentServiceImpl(
    private val attachmentRepository: AttachmentRepository
): AttachmentService {
    @Transactional(readOnly = true)
    override fun getAttachment(attachmentID: Long): AttachmentDTO {
        return attachmentRepository.findByIdOrNull(attachmentID)?.toDTO()?:
            throw AttachmentNotFoundException("Attachment with id '${attachmentID}' not found")
    }

    override fun addAttachment(attachmentDTO: AttachmentDTO): Long {
        return attachmentRepository.save(attachmentDTO.toNewAttachment()).attachmentId!!
    }
}