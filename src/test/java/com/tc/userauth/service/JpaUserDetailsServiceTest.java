package com.tc.userauth.service;

import static com.tc.userauth.testdata.TestUserBuilder.userBuilder;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tc.userauth.exception.RestErrorResponseException;
import com.tc.userauth.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class JpaUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JpaUserDetailsService jpaUserDetailsService;

    @Test
    void loadUserByUsername_userNotFound_throwsUsernameNotFoundException() {
        final var user = userBuilder().build();

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        final var exception = assertThrows(UsernameNotFoundException.class, () ->
                jpaUserDetailsService.loadUserByUsername(user.getUsername()));

        assertThat(exception.getMessage()).isEqualTo("User with username [testUser] not found");
        verify(userRepository).findByUsername(anyString());
    }

    @Test
    void loadUserByUsername_userVerified_returnsUserDetails() {
        final var user = userBuilder().withVerifiedEmail().build();

        when(userRepository.findByUsername(eq(user.getUsername()))).thenReturn(Optional.of(user));

        final var userDetails = jpaUserDetailsService.loadUserByUsername(user.getUsername());

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(user.getUsername());
        assertThat(userDetails.getPassword()).isEqualTo(user.getPassword());
        verify(userRepository).findByUsername(eq(user.getUsername()));
    }

    @Test
    void loadUserByUsername_emailNotVerified_throwsException() {
        final var user = userBuilder().build();

        when(userRepository.findByUsername(eq(user.getUsername()))).thenReturn(Optional.of(user));

        final var exception = assertThrows(RestErrorResponseException.class, () ->
                jpaUserDetailsService.loadUserByUsername(user.getUsername()));

        assertEquals("Email verification required", exception.getBody().getDetail());
        assertEquals(user.getEmail(), requireNonNull(exception.getBody().getProperties()).get("email"));
    }

}