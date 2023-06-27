package it.polito.wa2.server

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ExceptionRestControllerAdvice : ResponseEntityExceptionHandler() {
    @ExceptionHandler(ForbiddenException::class)
    fun handleUnauthorized(e: ForbiddenException) = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.message!!)

    @ExceptionHandler(UnprocessableUserException::class)
    fun handleUnprocessableUser(e: UnprocessableUserException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.message!!)

    @ExceptionHandler(BadRequestUserException::class)
    fun handleUserBadRequest(e: BadRequestUserException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message!!)

    /************** Product exception handlers **************/
    @ExceptionHandler(ProductNotFoundException::class)
    fun handleProductNotFound(e: ProductNotFoundException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message!!)

    @ExceptionHandler(UnprocessableProductException::class)
    fun handleUnprocessableProduct(e: UnprocessableProductException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.message!!)

    @ExceptionHandler(DuplicateProductException::class)
    fun handleDuplicateProduct(e: DuplicateProductException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message!!)

    @ExceptionHandler(BadRequestProductException::class)
    fun handleBadRequestProduct(e: BadRequestProductException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message!!)

    /************** Profile exception handlers **************/
    @ExceptionHandler(ProfileNotFoundException::class)
    fun handleProfileNotFound(e: ProfileNotFoundException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message!!)

    @ExceptionHandler(UnprocessableProfileException::class)
    fun handleUnprocessableProfile(e: UnprocessableProfileException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.message!!)

    @ExceptionHandler(DuplicateProfileException::class)
    fun handleDuplicateProfile(e: DuplicateProfileException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message!!)

    @ExceptionHandler(BadRequestProfileException::class)
    fun handleProfileBadRequest(e: BadRequestProfileException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message!!)

    @ExceptionHandler(LoginFailedException::class)
    fun handleLoginFailed(e: LoginFailedException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.message!!)

    /************** Attachment exception handlers **************/
    @ExceptionHandler(AttachmentNotFoundException::class)
    fun handleAttachmentNotFound(e: AttachmentNotFoundException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message!!)

    @ExceptionHandler(UnprocessableAttachmentException::class)
    fun handleUnprocessableAttachment(e: UnprocessableAttachmentException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.message!!)

    /************** Ticket exception handlers **************/
    @ExceptionHandler(TicketNotFoundException::class)
    fun handleTicketNotFound(e: TicketNotFoundException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message!!)

    @ExceptionHandler(UnprocessableTicketException::class)
    fun handleUnprocessableTicket(e: UnprocessableTicketException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.message!!)

    @ExceptionHandler(BadRequestFilterException::class)
    fun handleFilterBadRequest(e: BadRequestFilterException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message!!)

    /************** Message exception handlers **************/
    @ExceptionHandler(UnprocessableMessageException::class)
    fun handleUnprocessableMessage(e: UnprocessableMessageException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.message!!)

    @ExceptionHandler(BadRequestMessageException::class)
    fun handleMessageBadRequest(e: BadRequestMessageException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message!!)

    @ExceptionHandler(UnauthorizedMessageException::class)
    fun handleMessageUnauthorized(e: UnauthorizedMessageException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.message!!)

    /************** Category exception handlers **************/
    @ExceptionHandler(CategoryNotFoundException::class)
    fun handleCategoryNotFound(e: CategoryNotFoundException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message!!)

    /************** Brand exception handlers **************/
    @ExceptionHandler(BrandNotFoundException::class)
    fun handleBrandNotFound(e: BrandNotFoundException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message!!)

    @ExceptionHandler(UnprocessableBrandException::class)
    fun handleUnprocessableBrand(e: UnprocessableBrandException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.message!!)

    @ExceptionHandler(DuplicateBrandException::class)
    fun handleDuplicateBrand(e: DuplicateBrandException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message!!)

    /************** Item exception handlers **************/
    @ExceptionHandler(ItemNotFoundException::class)
    fun handleItemNotFound(e: ItemNotFoundException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message!!)

    @ExceptionHandler(UnprocessableItemException::class)
    fun handleUnprocessableItem(e: UnprocessableItemException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.message!!)

    @ExceptionHandler(DuplicateItemException::class)
    fun handleDuplicateItem(e: DuplicateItemException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message!!)

    @ExceptionHandler(BadRequestItemException::class)
    fun handleBrandBadRequest(e: BadRequestItemException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message!!)

    /************** Address exception handlers **************/
    @ExceptionHandler(AddressNotFoundException::class)
    fun handleAddressNotFound(e: AddressNotFoundException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message!!)

    @ExceptionHandler(DuplicateAddressException::class)
    fun handleDuplicateAddress(e: DuplicateAddressException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message!!)

    @ExceptionHandler(UnprocessableAddressException::class)
    fun handleUnprocessableAddress(e: UnprocessableAddressException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.message!!)
    @ExceptionHandler(UnprocessableMailException::class)
    fun handleUnprocessableEmail(e: UnprocessableMailException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.message!!)
}

class ProductNotFoundException(message: String) : RuntimeException(message)
class UnprocessableProductException(message: String) : RuntimeException(message)
class BadRequestProductException(message: String) : RuntimeException(message)
class DuplicateProductException(message: String) : RuntimeException(message)
class ProfileNotFoundException(message: String) : RuntimeException(message)
class UnprocessableProfileException(message: String) : RuntimeException(message)
class DuplicateProfileException(message: String) : RuntimeException(message)
class BadRequestProfileException(message: String) : RuntimeException(message)
class AttachmentNotFoundException(message: String) : RuntimeException(message)
class UnprocessableAttachmentException(message: String) : RuntimeException(message)
class TicketNotFoundException(message: String) : RuntimeException(message)
class UnprocessableTicketException(message: String) : RuntimeException(message)
class UnprocessableMessageException(message: String) : RuntimeException(message)
class BadRequestMessageException(message: String) : RuntimeException(message)
class UnauthorizedMessageException(message: String) : RuntimeException(message)
class BadRequestFilterException(message: String) : RuntimeException(message)
class LoginFailedException(message: String) : RuntimeException(message)
class ForbiddenException(message: String) : RuntimeException(message)
class UnprocessableUserException(message: String) : RuntimeException(message)
class BadRequestUserException(message: String) : RuntimeException(message)
class CategoryNotFoundException(message: String) : RuntimeException(message)
class BrandNotFoundException(message: String) : RuntimeException(message)
class UnprocessableBrandException(message: String) : RuntimeException(message)
class BadRequestBrandException(message: String) : RuntimeException(message)
class DuplicateBrandException(message: String) : RuntimeException(message)
class ItemNotFoundException(message: String) : RuntimeException(message)
class UnprocessableItemException(message: String) : RuntimeException(message)
class BadRequestItemException(message: String) : RuntimeException(message)
class DuplicateItemException(message: String) : RuntimeException(message)
class AddressNotFoundException(message: String) : RuntimeException(message)
class DuplicateAddressException(message: String) : RuntimeException(message)
class UnprocessableAddressException(message: String) : RuntimeException(message)
class UnprocessableMailException(message: String) : RuntimeException(message)