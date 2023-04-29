package it.polito.wa2.server.ticketing.attachment


import org.springframework.stereotype.Service

@Service
class AttachmentServiceImpl(
    private val attachmentRepository: AttachmentRepository
): AttachmentService {}