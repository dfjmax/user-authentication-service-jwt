package com.tc.userauth.service;

import static com.tc.userauth.exception.ErrorType.RESOURCE_ALREADY_EXISTS;
import static com.tc.userauth.exception.ProblemDetailBuilder.forStatusAndDetail;
import static org.springframework.http.HttpStatus.CONFLICT;

import com.tc.userauth.entity.User;
import com.tc.userauth.exception.RestErrorResponseException;
import com.tc.userauth.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(User user) {
        final var errors = new HashMap<String, List<String>>();

        if (userRepository.existsByEmail(user.getEmail())) {
            errors.put("email", List.of("Email is already taken"));
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            errors.put("username", List.of("Username is already taken"));
        }

        if (!errors.isEmpty()) {
            throw new RestErrorResponseException(forStatusAndDetail(CONFLICT, "Request validation failed")
                    .withProperty("errors", errors)
                    .withErrorType(RESOURCE_ALREADY_EXISTS)
                    .build()
            );
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

}