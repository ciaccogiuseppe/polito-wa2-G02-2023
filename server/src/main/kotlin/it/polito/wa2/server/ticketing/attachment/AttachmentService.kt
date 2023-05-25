package it.polito.wa2.server.ticketing.attachment

interface AttachmentService {
    fun getAttachment(attachmentID: Long, userEmail: String) : AttachmentDTO

    fun addAttachment(attachmentDTO: AttachmentDTO): Long
}