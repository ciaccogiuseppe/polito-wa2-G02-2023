package it.polito.wa2.server

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ExceptionRestControllerAdvice: ResponseEntityExceptionHandler() {
    /************** Product exception handlers **************/
    @ExceptionHandler(ProductNotFoundException::class)
    fun handleProductNotFound(e: ProductNotFoundException) = ProblemDetail.forStatusAndDetail( HttpStatus.NOT_FOUND, e.message!! )

    @ExceptionHandler(UnprocessableProductException::class)
    fun handleUnprocessableProduct(e: UnprocessableProductException) = ProblemDetail.forStatusAndDetail( HttpStatus.UNPROCESSABLE_ENTITY, e.message!! )

    @ExceptionHandler(DuplicateProductException::class)
    fun handleDuplicateProduct(e: DuplicateProductException) = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message!! )

    /************** Profile exception handlers **************/
    @ExceptionHandler(ProfileNotFoundException::class)
    fun handleProfileNotFound(e: ProfileNotFoundException) = ProblemDetail.forStatusAndDetail( HttpStatus.NOT_FOUND, e.message!! )

    @ExceptionHandler(UnprocessableProfileException::class)
    fun handleUnprocessableProfile(e: UnprocessableProfileException) = ProblemDetail.forStatusAndDetail( HttpStatus.UNPROCESSABLE_ENTITY, e.message!! )

    @ExceptionHandler(DuplicateProfileException::class)
    fun handleDuplicateProfile(e: DuplicateProfileException) = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message!! )

    @ExceptionHandler(BadRequestProfileException::class)
    fun handleProfileBadRequest(e: BadRequestProfileException) = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message!! )

    /************** Attachment exception handlers **************/
    @ExceptionHandler(AttachmentNotFoundException::class)
    fun handleAttachmentNotFound(e: AttachmentNotFoundException) = ProblemDetail.forStatusAndDetail( HttpStatus.NOT_FOUND, e.message!! )

    @ExceptionHandler(UnprocessableAttachmentException::class)
    fun handleUnprocessableAttachment(e: UnprocessableAttachmentException) = ProblemDetail.forStatusAndDetail( HttpStatus.UNPROCESSABLE_ENTITY, e.message!! )

    /************** Ticket exception handlers **************/
    @ExceptionHandler(TicketNotFoundException::class)
    fun handleTicketNotFound(e: TicketNotFoundException) = ProblemDetail.forStatusAndDetail( HttpStatus.NOT_FOUND, e.message!! )

    @ExceptionHandler(UnprocessableTicketException::class)
    fun handleTicketNotFound(e: UnprocessableTicketException) = ProblemDetail.forStatusAndDetail( HttpStatus.UNPROCESSABLE_ENTITY, e.message!! )


    /************** Message exception handlers **************/
    @ExceptionHandler(UnprocessableMessageException::class)
    fun handleUnprocessableMessage(e: UnprocessableMessageException) = ProblemDetail.forStatusAndDetail( HttpStatus.UNPROCESSABLE_ENTITY, e.message!! )

    @ExceptionHandler(BadRequestMessageException::class)
    fun handleMessageBadRequest(e: BadRequestMessageException) = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message!! )

    @ExceptionHandler(UnauthorizedMessageException::class)
    fun handleMessageBadRequest(e: UnauthorizedMessageException) = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.message!! )
}

class ProductNotFoundException(message: String): RuntimeException(message)
class UnprocessableProductException(message: String): RuntimeException(message)
class DuplicateProductException(message: String): RuntimeException(message)
class ProfileNotFoundException(message: String): RuntimeException(message)
class UnprocessableProfileException(message: String): RuntimeException(message)
class DuplicateProfileException(message: String): RuntimeException(message)
class BadRequestProfileException(message: String): RuntimeException(message)
class AttachmentNotFoundException(message: String): RuntimeException(message)
class UnprocessableAttachmentException(message: String): RuntimeException(message)
class TicketNotFoundException(message: String): RuntimeException(message)
class UnprocessableTicketException(message: String): RuntimeException(message)
class UnprocessableMessageException(message: String): RuntimeException(message)
class BadRequestMessageException(message: String): RuntimeException(message)
class UnauthorizedMessageException(message: String): RuntimeException(message)