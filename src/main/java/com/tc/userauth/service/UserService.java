package com.tc.userauth.service;

import static com.tc.userauth.exception.ErrorType.ACCOUNT_UNAVAILABLE;
import static com.tc.userauth.exception.ProblemDetailBuilder.forStatusAndDetail;
import static org.springframework.http.HttpStatus.GONE;

import com.tc.userauth.entity.User;
import com.tc.userauth.exception.RestErrorResponseException;
import com.tc.userauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserByUsername(final String username) {
        return userRepository.findByUsername(username).orElseThrow(() ->
                new RestErrorResponseException(forStatusAndDetail(GONE, "The user account has been deleted or inactivated")
                        .withErrorType(ACCOUNT_UNAVAILABLE)
                        .build()
                )
        );
    }
}