package com.vodafoneziggo.assignment.order.api;

import com.vodafoneziggo.assignment.order.contract.model.ErrorResponse;
import com.vodafoneziggo.assignment.order.service.OrderService.DuplicateOrderException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import com.vodafoneziggo.assignment.order.service.OrderService.EmailNotFoundException;
import com.vodafoneziggo.assignment.order.integration.ReqResClient.ReqResUnavailableException;

/**
 * Centralized exception handling for all REST controllers.
 * Keeps controllers clean and ensures consistent error responses.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * Handles validation errors coming from request body or parameters.
     * This covers Valid failures and constraint violations.
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> badRequest(Exception ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse("BAD_REQUEST", "Invalid request"));
    }

    /**
     * Thrown when the same customer tries to order the same product twice.
     */
    @ExceptionHandler(DuplicateOrderException.class)
    public ResponseEntity<ErrorResponse> duplicate() {
        return ResponseEntity.status(409).body(new ErrorResponse("DUPLICATE_ORDER", "Customer has already ordered this product"));
    }

    /**
     * Final safety net for any unhandled exception.
     * Prevents leaking internal errors to API consumers.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> generic(Exception ex) {
        return ResponseEntity.status(500).body(new ErrorResponse("INTERNAL_ERROR", "Unexpected error"));
    }

    /**
     * Thrown when the provided email does not exist in the external user system.
     */
    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<ErrorResponse> emailNotFound() {
        return ResponseEntity.unprocessableEntity()
                .body(new ErrorResponse(
                        "EMAIL_NOT_FOUND",
                        "Email does not exist in external user system"
                ));
    }

    /**
     * Handles failures when the external ReqRes service is unavailable.
     * Exposed as 502 since this is a downstream dependency failure.
     */
    @ExceptionHandler(ReqResUnavailableException.class)
    public ResponseEntity<ErrorResponse> externalServiceDown(ReqResUnavailableException ex) {
        return ResponseEntity.status(502)
                .body(new ErrorResponse(
                        "EXTERNAL_SERVICE_ERROR",
                        ex.getMessage()
                ));
    }

    /**
     * Handles missing required query parameters in requests.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> missingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        "BAD_REQUEST",
                        "Required query parameter '" + ex.getParameterName() + "' is missing"
                ));
    }
}
