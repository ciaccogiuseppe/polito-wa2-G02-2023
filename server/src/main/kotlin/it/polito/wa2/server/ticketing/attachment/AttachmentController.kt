package it.polito.wa2.server.ticketing.attachment

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController


@RestController
class AttachmentController (private val attachmentService : AttachmentService) {
    @GetMapping("/API/attachment/{attachmentId}")
    fun getAttachment(@PathVariable attachmentId: Long) : AttachmentDTO {
        return attachmentService.getAttachment(attachmentId)
    }
}