package com.tc.userauth.exception;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ExtendWith(MockitoExtension.class)
class RestExceptionHandlerTest {

    @InjectMocks
    private RestExceptionHandler restExceptionHandler;

    @Mock
    private WebRequest webRequest;

    @Mock
    private BindingResult bindingResult;

    @Test
    void handleMethodArgumentNotValid_invalidArguments_returnsBadRequest() {
        final var fieldErrors = List.of(
                new FieldError("objectName", "email", "Invalid email"),
                new FieldError("objectName", "username", "Username already taken")
        );

        final var methodArgumentNotValidException = mock(MethodArgumentNotValidException.class);

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        final var response = restExceptionHandler.handleMethodArgumentNotValid(
                methodArgumentNotValidException, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest
        );

        final var problemDetail = (ProblemDetail) requireNonNull(response).getBody();
        assertNotNull(problemDetail);
        assertEquals("Request validation failed", problemDetail.getDetail());
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
        assertNotNull(problemDetail.getProperties());
        assertNotNull(problemDetail.getProperties().get("errors"));
    }

    @Test
    void handleMissingRequestCookieException_missingCookie_returnsBadRequest() {
        final var methodArgumentTypeMismatchException = mock(MissingRequestCookieException.class);

        final var response = restExceptionHandler.handleMissingRequestCookieException(
                methodArgumentTypeMismatchException
        );

        final var problemDetail = requireNonNull(response).getBody();
        assertNotNull(problemDetail);
        assertEquals("Required cookie is missing", problemDetail.getDetail());
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
    }

    @Test
    void handleMethodArgumentTypeMismatchException_invalidArguments_returnsBadRequest() {
        final var methodArgumentTypeMismatchException = mock(MethodArgumentTypeMismatchException.class);

        when(methodArgumentTypeMismatchException.getName()).thenReturn("email");

        final var response = restExceptionHandler.handleMethodArgumentTypeMismatchException(
                methodArgumentTypeMismatchException
        );

        final var problemDetail = requireNonNull(response).getBody();
        assertNotNull(problemDetail);
        assertEquals("Parameter [email] contains an invalid value", problemDetail.getDetail());
        assertEquals(HttpStatus.BAD_REQUEST.value(), problemDetail.getStatus());
    }

    @Test
    void handleAuthenticationException_invalidCredentials_returnsUnauthorized() {
        final var authenticationException = mock(AuthenticationException.class);
        when(authenticationException.getMessage()).thenReturn("Unauthorized access");

        final var response = restExceptionHandler.handleAuthenticationException(authenticationException);

        final var problemDetail = response.getBody();
        assertNotNull(problemDetail);
        assertEquals("Unauthorized access", problemDetail.getDetail());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), problemDetail.getStatus());
    }

    @Test
    void handleConstraintViolationException_constraintViolation_returnsConflict() {
        final var constraintViolationException = mock(ConstraintViolationException.class);

        final var response = restExceptionHandler.handleConstraintViolationException(
                constraintViolationException, webRequest
        );

        final var problemDetail = response.getBody();
        assertNotNull(problemDetail);
        assertEquals("Error while processing the request", problemDetail.getDetail());
        assertEquals(HttpStatus.CONFLICT.value(), problemDetail.getStatus());
    }

    @Test
    void handleGenericException_unexpectedError_returnsInternalServerError() {
        final var genericException = new Exception("Something went wrong");

        final var response = restExceptionHandler.handleGenericException(genericException);

        final var problemDetail = response.getBody();
        assertNotNull(problemDetail);
        assertEquals("An unexpected error occurred", problemDetail.getDetail());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problemDetail.getStatus());
    }
}