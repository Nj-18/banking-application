package com.banking.demo.exceptions;

import com.banking.demo.dtos.ErrorResponseDTO;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccountNotFound(AccountNotFoundException ex, HttpServletRequest request) {

        ErrorResponseDTO response = buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(SameAccountTransferException.class)
    public ResponseEntity<ErrorResponseDTO> handleSameAccount(SameAccountTransferException ex, HttpServletRequest request) {

        ErrorResponseDTO response = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccountInactiveException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccountInactive(
            AccountInactiveException ex,
            HttpServletRequest request) {

        ErrorResponseDTO response = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleUsernameAlreadyExists(
            UsernameAlreadyExistsException ex,
            HttpServletRequest request) {

        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDTO> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {

        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleCustomerNotFound(CustomerNotFoundException ex, HttpServletRequest request) {

        ErrorResponseDTO response = buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidAmount(InvalidAmountException ex, HttpServletRequest request) {

        ErrorResponseDTO response = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        ErrorResponseDTO response = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request) {

        ErrorResponseDTO response = buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(UserNotEnabledException.class)
    public ResponseEntity<ErrorResponseDTO> handleUserNotEnabled(
            UserNotEnabledException ex,
            HttpServletRequest request) {

        ErrorResponseDTO response = buildErrorResponse(
                HttpStatus.FORBIDDEN,
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponseDTO response = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong. Please try again later.",
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponseDTO> handleInsufficientBalance(
            InsufficientBalanceException ex,
            HttpServletRequest request) {

        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponseDTO> handleServiceUnavailable(
            ServiceUnavailableException ex,
            HttpServletRequest request) {

        ErrorResponseDTO response = buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                ex.getMessage(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler({
            CallNotPermittedException.class,
            RequestNotPermitted.class,
            BulkheadFullException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleResilienceLimits(
            RuntimeException ex,
            HttpServletRequest request) {

        String message;
        if (ex instanceof CallNotPermittedException) {
            message = "Banking service circuit is open. Please retry in a few seconds.";
        } else if (ex instanceof RequestNotPermitted) {
            message = "Too many requests. Please slow down and try again.";
        } else {
            message = "Transfer capacity is full. Please try again shortly.";
        }

        ErrorResponseDTO response = buildErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS,
                message,
                request.getRequestURI());

        // Circuit open is better as 503; rate/bulkhead as 429
        HttpStatus status = ex instanceof CallNotPermittedException
                ? HttpStatus.SERVICE_UNAVAILABLE
                : HttpStatus.TOO_MANY_REQUESTS;

        response.setStatus(status.value());
        response.setError(status.getReasonPhrase());

        return ResponseEntity.status(status).body(response);
    }


    private ErrorResponseDTO buildErrorResponse(
            HttpStatus status,
            String message,
            String path) {

        return ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
    }
}