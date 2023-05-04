package it.polito.wa2.server.ticketing.attachment


import it.polito.wa2.server.AttachmentNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class AttachmentServiceImpl(
    private val attachmentRepository: AttachmentRepository
): AttachmentService {
    override fun getAttachment(attachmentID: String): AttachmentDTO {
        return attachmentRepository.findByIdOrNull(attachmentID)?.toDTO()?:
            throw AttachmentNotFoundException("Attachment with id '${attachmentID}' not found")
    }
}