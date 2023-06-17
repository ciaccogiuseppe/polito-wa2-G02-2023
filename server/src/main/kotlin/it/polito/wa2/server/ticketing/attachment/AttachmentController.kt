package it.polito.wa2.server.ticketing.attachment

import io.micrometer.observation.annotation.Observed
import it.polito.wa2.server.UnprocessableAttachmentException
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.CrossOrigin
import java.security.Principal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController


@CrossOrigin(origins =["http://localhost:3001"])
@RestController
@Observed
class AttachmentController (private val attachmentService : AttachmentService) {
    @GetMapping("/API/attachment/{attachmentId}")
    fun getAttachment(principal: Principal, @PathVariable attachmentId: Long) : AttachmentDTO {
        val token: JwtAuthenticationToken = principal as JwtAuthenticationToken
        val userEmail = token.tokenAttributes["email"] as String
        if(attachmentId <= 0)
            throw UnprocessableAttachmentException("Wrong attachment id value")
        return attachmentService.getAttachment(attachmentId, userEmail)
    }

    @GetMapping("/API/manager/attachment/{attachmentId}")
    fun getAttachmentManager(principal: Principal, @PathVariable attachmentId: Long) : AttachmentDTO {
        if(attachmentId <= 0)
            throw UnprocessableAttachmentException("Wrong attachment id value")
        return attachmentService.getAttachmentManager(attachmentId)
    }
}