package com.tc.userauth.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tc.userauth.config.OtpConfig.OtpConfigProperties;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;


    private OtpService otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpService(
                new OtpConfigProperties("test:%s", Duration.ZERO, 10),
                redisTemplate,
                PASSWORD_ENCODER
        );
    }

    @Test
    void generateAndStoreOtp_validId_otpStored() {
        final var id = UUID.randomUUID();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        otpService.generateAndStoreOtp(id);
        verify(valueOperations).set(anyString(), anyString(), eq(Duration.ZERO));
    }

    @Test
    void isOtpValid_validOtp_returnsTrue() {
        final var id = UUID.randomUUID();
        final var expectedOtp = "some-otp-value";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("test:" + id)).thenReturn(PASSWORD_ENCODER.encode(expectedOtp));

        assertTrue(otpService.isOtpValid(id, expectedOtp));
    }

    @Test
    void isOtpValid_noOtp_returnsFalse() {
        final var id = UUID.randomUUID();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("test:" + id)).thenReturn(PASSWORD_ENCODER.encode("another-otp"));

        assertFalse(otpService.isOtpValid(id, "test"));
    }

    @Test
    void isOtpValid_invalidOtp_returnsFalse() {
        final var id = UUID.randomUUID();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("test:" + id)).thenReturn(null);

        assertFalse(otpService.isOtpValid(id, "test"));
    }

    @Test
    void deleteOtp_validId_otpDeleted() {
        final var id = UUID.randomUUID();

        otpService.deleteOtp(id);

        verify(redisTemplate).delete("test:" + id);
    }

}