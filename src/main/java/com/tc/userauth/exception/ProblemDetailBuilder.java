package com.tc.userauth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

public class ProblemDetailBuilder {

    private final ProblemDetail problemDetail;

    private ProblemDetailBuilder(HttpStatusCode status) {
        this.problemDetail = ProblemDetail.forStatus(status);
    }

    public static ProblemDetailBuilder forStatus(HttpStatus status) {
        return new ProblemDetailBuilder(status);
    }

    public static ProblemDetailBuilder forStatusAndDetail(HttpStatusCode status, String detail) {
        ProblemDetailBuilder builder = new ProblemDetailBuilder(status);
        builder.problemDetail.setDetail(detail);
        return builder;
    }

    public ProblemDetailBuilder withErrorType(ErrorType type) {
        this.problemDetail.setType(type.getUri());
        return this;
    }

    public ProblemDetailBuilder withProperty(String name, Object value) {
        this.problemDetail.setProperty(name, value);
        return this;
    }

    public ProblemDetail build() {
        return this.problemDetail;
    }

}
