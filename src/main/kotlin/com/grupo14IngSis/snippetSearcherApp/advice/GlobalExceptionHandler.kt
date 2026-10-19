package com.grupo14IngSis.snippetSearcherApp.advice

import com.grupo14IngSis.snippetSearcherApp.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import org.apache.coyote.BadRequestException
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.resource.NoResourceFoundException

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

    /**
     * Ruta inexistente (404), método no soportado (405), Content-Type no soportado (415) o body ilegible
     * (400): son errores del cliente, no del servidor. Sin esto el handler genérico de abajo los convertía en 500.
     */
    @ExceptionHandler(
        HttpRequestMethodNotSupportedException::class,
        NoResourceFoundException::class,
        HttpMediaTypeNotSupportedException::class,
        HttpMessageNotReadableException::class,
    )
    fun handleRoutingErrors(
        ex: Exception,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val status =
            when (ex) {
                is HttpRequestMethodNotSupportedException -> HttpStatus.METHOD_NOT_ALLOWED
                is HttpMediaTypeNotSupportedException -> HttpStatus.UNSUPPORTED_MEDIA_TYPE
                is HttpMessageNotReadableException -> HttpStatus.BAD_REQUEST
                else -> HttpStatus.NOT_FOUND
            }
        logger.warn("[SNIPPET-SEARCHER] ${httpRequest.method} ${httpRequest.requestURI} - ${status.value()}: ${ex.message}")
        return ResponseEntity(ErrorResponse(status.value(), status.reasonPhrase, ex.message ?: ""), status)
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
