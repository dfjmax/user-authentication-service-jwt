package com.tc.userauth.controller;

import static com.tc.userauth.model.AuthTokens.REFRESH_TOKEN_COOKIE_NAME;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tc.userauth.dto.AuthenticationRequestDto;
import com.tc.userauth.model.AuthTokens;
import com.tc.userauth.service.AuthenticationService;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.security.authentication.BadCredentialsException;

@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest extends ControllerTest {

    private static final String JWT_TOKEN = "mockJwtToken";

    private static final String REFRESH_TOKEN = UUID.randomUUID().toString();

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    void authenticate_validCredentials_returnsToken() throws Exception {
        when(authenticationService.authenticate(anyString(), anyString())).thenReturn(new AuthTokens(JWT_TOKEN, REFRESH_TOKEN, Duration.ofHours(1)));

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAuthenticationRequestDto())))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(REFRESH_TOKEN_COOKIE_NAME))
                .andExpect(jsonPath("$.accessToken").value(JWT_TOKEN));

        verify(authenticationService).authenticate(anyString(), anyString());
    }

    @Test
    void authenticate_invalidCredentials_returnsUnauthorized() throws Exception {
        when(authenticationService.authenticate(anyString(), anyString())).thenThrow(new BadCredentialsException("Login failed"));

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAuthenticationRequestDto())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Login failed"));

        verify(authenticationService).authenticate(anyString(), anyString());
    }

    @Test
    void refreshToken_validRefreshToken_returnsNewAccessToken() throws Exception {
        final var jwtToken = "new-jwt-token";
        final var authTokens = new AuthTokens(jwtToken, REFRESH_TOKEN, Duration.ofHours(1));

        when(authenticationService.refreshToken(eq(REFRESH_TOKEN))).thenReturn(authTokens);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new MockCookie(REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(jwtToken));

        verify(authenticationService).refreshToken(eq(REFRESH_TOKEN));
    }

    @Test
    void refreshToken_invalidRefreshToken_throwsAuthenticationException() throws Exception {
        when(authenticationService.refreshToken(eq(REFRESH_TOKEN)))
                .thenThrow(new BadCredentialsException("Invalid refresh token"));

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new MockCookie(REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid refresh token"));

        verify(authenticationService).refreshToken(eq(REFRESH_TOKEN));
    }

    @Test
    void revokeToken_validToken_returnsNoContent() throws Exception {
        mockMvc.perform(post("/api/auth/sign-out")
                        .cookie(new MockCookie(REFRESH_TOKEN_COOKIE_NAME, REFRESH_TOKEN))
                        .param("refreshToken", REFRESH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(authenticationService).revokeRefreshToken(eq(REFRESH_TOKEN));
    }

    private AuthenticationRequestDto newAuthenticationRequestDto() {
        return new AuthenticationRequestDto("testUser", "password123");
    }

}