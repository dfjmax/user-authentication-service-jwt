package com.tc.userauth.exception;

import static java.net.URI.create;

import java.net.URI;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorType {

    UNAUTHORIZED(create("errors/unauthorized")),
    FORBIDDEN(create("errors/forbidden")),
    ACCOUNT_UNAVAILABLE(create("errors/account-unavailable")),
    REQUEST_VALIDATION_FAILED(create("errors/request-validation-failed")),
    RESOURCE_ALREADY_EXISTS(create("errors/resource-already-exists")),
    EMAIL_VERIFICATION_REQUIRED(create("errors/email-verification-required")),
    EMAIL_VERIFICATION_FAILED(create("errors/email-verification-failed")),
    EMAIL_ALREADY_VERIFIED(create("errors/email-already-verified")),
    UNKNOWN_SERVER_ERROR(create("errors/unknown-server-error"));

    private final URI uri;
}