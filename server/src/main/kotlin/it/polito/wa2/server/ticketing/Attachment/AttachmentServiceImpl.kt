package it.polito.wa2.server.ticketing.Attachment


import org.springframework.stereotype.Service

@Service
class AttachmentServiceImpl(
    private val attachmentRepository: AttachmentRepository
): AttachmentService {}