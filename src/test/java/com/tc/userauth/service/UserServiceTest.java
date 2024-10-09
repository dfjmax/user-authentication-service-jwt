package com.tc.userauth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.GONE;

import com.tc.userauth.entity.User;
import com.tc.userauth.exception.RestErrorResponseException;
import com.tc.userauth.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Mock
    private User user;

    @Test
    void getUserByUsername_existingUsername_returnsUser() {
        final var username = "testUser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        final var user = userService.getUserByUsername(username);

        assertNotNull(user);
        verify(userRepository).findByUsername(username);
    }

    @Test
    void getUserByUsername_nonExistingUsername_throwsException() {
        final var username = "unknownUser";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        final var exception = assertThrows(RestErrorResponseException.class, () -> userService.getUserByUsername(username));

        assertEquals("The user account has been deleted or inactivated", exception.getBody().getDetail());
        assertEquals(GONE, exception.getStatusCode());
    }
}