package com.tc.userauth.service;

import static com.tc.userauth.testdata.TestUserBuilder.userBuilder;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tc.userauth.entity.User;
import com.tc.userauth.exception.RestErrorResponseException;
import com.tc.userauth.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @Test
    void sendEmailVerificationOtp_validUser_sendsOtp() {
        final var user = userBuilder().withRandomId().build();
        final var token = UUID.randomUUID().toString();

        when(otpService.generateAndStoreOtp(eq(user.getId()))).thenReturn(token);

        emailVerificationService.sendEmailVerificationOtp(user.getId(), user.getEmail());

        verify(otpService).generateAndStoreOtp(eq(user.getId()));
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void resendEmailVerificationOtp_unverifiedEmail_resendsOtp() {
        final var user = userBuilder().withRandomId().build();

        when(otpService.generateAndStoreOtp(eq(user.getId()))).thenReturn("token");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(of(user));

        emailVerificationService.resendEmailVerificationOtp(user.getEmail());

        verify(userRepository).findByEmail(user.getEmail());
        verify(mailSender).send(any(SimpleMailMessage.class));
        verify(otpService).generateAndStoreOtp(eq(user.getId()));
    }

    @Test
    void verifyEmail_validToken_verifiesEmailOtp() {
        final var token = UUID.randomUUID().toString();
        final var user = userBuilder().withRandomId().build();

        when(otpService.isOtpValid(eq(user.getId()), anyString())).thenReturn(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(of(user));

        emailVerificationService.verifyEmailOtp(user.getEmail(), token);

        assertTrue(user.isEmailVerified());
        verify(userRepository).findByEmail(user.getEmail());
        verify(otpService).deleteOtp(eq(user.getId()));
    }

    @Test
    void verifyEmail_Otp_invalidToken_throwsException() {
        final var token = UUID.randomUUID().toString();
        final var user = userBuilder().withRandomId().build();

        when(userRepository.findByEmail("random@email.com")).thenReturn(empty());

        final var exception = assertThrows(RestErrorResponseException.class, () ->
                emailVerificationService.verifyEmailOtp("random@email.com", token));

        assertThat(exception.getBody().getDetail()).isEqualTo("Invalid email or token");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void verifyEmail_invalidEmail_Otp_throwsException() {
        final var token = UUID.randomUUID().toString();

        final var exception = assertThrows(RestErrorResponseException.class, () ->
                emailVerificationService.verifyEmailOtp("random@email.com", token));

        assertThat(exception.getBody().getDetail()).isEqualTo("Invalid email or token");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void verifyEmail_Otp_alreadyVerified_throwsException() {
        final var token = UUID.randomUUID().toString();

        final var user = userBuilder().withRandomId().withVerifiedEmail().build();

        when(otpService.isOtpValid(eq(user.getId()), anyString())).thenReturn(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(of(user));

        final var exception = assertThrows(RestErrorResponseException.class, () -> emailVerificationService.verifyEmailOtp(user.getEmail(), token));
        assertThat(exception.getBody().getDetail()).isEqualTo("Email is already verified");

        verify(userRepository, never()).save(any(User.class));
    }

}