package com.tc.userauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailVerificationRequestDto(
        @NotBlank(message = "Email is required")
        @Email
        String email,

        @NotBlank(message = "Otp is required")
        String otp
) {
}