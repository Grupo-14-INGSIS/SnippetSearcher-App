package com.grupo14IngSis.snippetSearcherApp.advice

import com.grupo14IngSis.snippetSearcherApp.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import org.apache.coyote.BadRequestException
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(
        ex: BadRequestException,
        request: WebRequest,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val requestId = MDC.get("requestId") ?: "unknown"
        val method = httpRequest.method
        val uri = httpRequest.requestURI

        logger.warn("[SNIPPET-SEARCHER] Request $requestId - $method $uri - Bad request: ${ex.message}")

        val errorResponse =
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                message = "Invalid request",
                details = ex.message ?: "No details available",
            )

        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(
        ex: Exception,
        request: WebRequest,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val requestId = MDC.get("requestId") ?: "unknown"
        val method = httpRequest.method
        val uri = httpRequest.requestURI

        logger.error("[SNIPPET-SEARCHER] Request $requestId - $method $uri - Unhandled exception occurred", ex)

        val errorResponse =
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                message = "An unexpected internal server error occurred.",
                details = ex.message ?: "No details available",
            )

        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
