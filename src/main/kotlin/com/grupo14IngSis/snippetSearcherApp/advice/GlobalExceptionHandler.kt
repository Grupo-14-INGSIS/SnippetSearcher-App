package com.grupo14IngSis.snippetSearcherApp.advice

import com.grupo14IngSis.snippetSearcherApp.dto.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(
        ex: Exception,
        request: WebRequest,
    ): ResponseEntity<ErrorResponse> {
        // Log the error with the full stack trace, as it's an unexpected system error.
        // At this point, the MDC still contains the `requestId`.
        logger.error("Unhandled exception occurred", ex)

        val errorResponse =
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                message = "An unexpected internal server error occurred.",
                details = ex.message ?: "No details available",
            )

        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
