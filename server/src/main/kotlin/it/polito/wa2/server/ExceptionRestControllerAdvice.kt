package it.polito.wa2.server

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionRestControllerAdvice {
    /************** Product exception handlers **************/
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ProductNotFoundException::class)
    fun handleProductNotFound(e: ProductNotFoundException) = ProblemDetail.forStatusAndDetail( HttpStatus.NOT_FOUND, e.message!! )

    @ResponseBody
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(UnprocessableProductException::class)
    fun handleProductNotFound(e: UnprocessableProductException) = ProblemDetail.forStatusAndDetail( HttpStatus.UNPROCESSABLE_ENTITY, e.message!! )

    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateProductException::class)
    fun handleDuplicateProduct(e: DuplicateProductException) = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message!! )

    /************** Profile exception handlers **************/
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ProfileNotFoundException::class)
    fun handleProductNotFound(e: ProfileNotFoundException) = ProblemDetail.forStatusAndDetail( HttpStatus.NOT_FOUND, e.message!! )

    @ResponseBody
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(UnprocessableProfileException::class)
    fun handleProductNotFound(e: UnprocessableProfileException) = ProblemDetail.forStatusAndDetail( HttpStatus.UNPROCESSABLE_ENTITY, e.message!! )
}

class ProductNotFoundException(message: String): RuntimeException(message) {}
class UnprocessableProductException(message: String): RuntimeException(message) {}
class DuplicateProductException(message: String): RuntimeException(message) {}
class ProfileNotFoundException(message: String): RuntimeException(message) {}
class UnprocessableProfileException(message: String): RuntimeException(message) {}