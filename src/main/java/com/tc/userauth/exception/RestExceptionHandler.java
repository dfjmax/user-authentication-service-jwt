package com.tc.userauth.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            final MethodArgumentNotValidException ex, @NonNull final HttpHeaders headers, @NonNull final HttpStatusCode status, @NonNull final WebRequest request
    ) {
        final Map<String, List<String>> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.computeIfAbsent(error.getField(), key -> new ArrayList<>()).add(error.getDefaultMessage());
        }

        final var problemDetail = ProblemDetailBuilder.forStatusAndDetail(status, "Request validation failed")
                .withErrorType(ErrorType.REQUEST_VALIDATION_FAILED)
                .withProperty("errors", errors)
                .build();

        return new ResponseEntity<>(problemDetail, status);
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ProblemDetail> handleMissingRequestCookieException(final MissingRequestCookieException ex) {
        final var problemDetail = ProblemDetailBuilder.forStatusAndDetail(BAD_REQUEST, "Required cookie is missing")
                .withErrorType(ErrorType.REQUEST_VALIDATION_FAILED)
                .build();

        return new ResponseEntity<>(problemDetail, BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException ex) {
        final var problemDetail = ProblemDetailBuilder.forStatusAndDetail(BAD_REQUEST, "Parameter [%s] contains an invalid value".formatted(ex.getName()))
                .withErrorType(ErrorType.REQUEST_VALIDATION_FAILED)
                .build();

        return new ResponseEntity<>(problemDetail, BAD_REQUEST);
    }


    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(final AuthenticationException ex) {
        final var problemDetail = ProblemDetailBuilder.forStatusAndDetail(UNAUTHORIZED, ex.getMessage())
                .withErrorType(ErrorType.UNAUTHORIZED)
                .build();

        if (log.isDebugEnabled()) {
            log.debug("Authorization exception stack trace: ", ex);
        }

        return new ResponseEntity<>(problemDetail, UNAUTHORIZED);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(final ConstraintViolationException ex, @NonNull final WebRequest request) {
        final var problemDetail = ProblemDetailBuilder.forStatusAndDetail(CONFLICT, "Error while processing the request")
                .withErrorType(ErrorType.RESOURCE_ALREADY_EXISTS)
                .build();

        return new ResponseEntity<>(problemDetail, CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(final Exception ex) {
        log.error("Unexpected error occurred", ex);

        final var problemDetail = ProblemDetailBuilder.forStatusAndDetail(INTERNAL_SERVER_ERROR, "An unexpected error occurred")
                .withErrorType(ErrorType.UNKNOWN_SERVER_ERROR)
                .build();

        return new ResponseEntity<>(problemDetail, INTERNAL_SERVER_ERROR);
    }

}