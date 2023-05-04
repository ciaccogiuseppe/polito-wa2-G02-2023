package it.polito.wa2.server.ticketing.attachment

interface AttachmentService {
    fun getAttachment(attachmentID: String) : AttachmentDTO;
}