package it.polito.wa2.server.ticketing.attachment

import it.polito.wa2.server.UnprocessableAttachmentException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController


@RestController
class AttachmentController (private val attachmentService : AttachmentService) {
    @GetMapping("/API/attachment/{attachmentId}")
    fun getAttachment(@PathVariable attachmentId: Long) : AttachmentDTO {
        if(attachmentId <= 0)
            throw UnprocessableAttachmentException("Wrong attachment id value")
        return attachmentService.getAttachment(attachmentId)
    }
}