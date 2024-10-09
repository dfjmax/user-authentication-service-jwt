package com.tc.userauth.config;

import com.tc.userauth.service.OtpService;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Setter
@Getter
@ConfigurationProperties(prefix = "otp")
public class OtpConfig {

    private OtpConfigProperties emailVerification;

    @Bean
    public OtpService emailVerificationOtpService(RedisTemplate<String, String> redisTemplate, PasswordEncoder passwordEncoder) {
        return new OtpService(emailVerification, redisTemplate, passwordEncoder);
    }

    public record OtpConfigProperties(String cachePrefix, Duration ttl, Integer length) {
    }

}