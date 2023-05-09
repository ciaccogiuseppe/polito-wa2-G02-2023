package it.polito.wa2.server.ticketing.attachment

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import org.springframework.data.repository.findByIdOrNull

data class AttachmentDTO(
    @field:Positive
    val attachmentId: Long?,
    @field:NotEmpty(message="attachment is mandatory")
    val attachment: ByteArray,
    @field:NotBlank(message="name is mandatory")
    val name: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AttachmentDTO

        if (attachmentId != other.attachmentId) return false
        if (!attachment.contentEquals(other.attachment)) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = attachmentId?.hashCode() ?: 0
        result = 31 * result + attachment.contentHashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}

fun Attachment.toDTO() : AttachmentDTO{
    return AttachmentDTO(attachmentId, attachment, name)
}

/*
fun AttachmentDTO.toAttachment(attachmentRepository: AttachmentRepository): Attachment{
    var attachmentObj = attachmentRepository.findByIdOrNull(attachmentId.toString())
    if(attachmentObj != null)
        return attachmentObj
    attachmentObj = Attachment()
    attachmentObj.attachmentId = attachmentId
    attachmentObj.name = name
    attachmentObj.attachment = attachment
    return attachmentObj
}
 */