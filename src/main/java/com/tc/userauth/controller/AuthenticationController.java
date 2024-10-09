package com.tc.userauth.controller;

import static com.tc.userauth.model.AuthTokens.REFRESH_TOKEN_COOKIE_NAME;
import static com.tc.userauth.util.CookieUtil.addCookie;
import static com.tc.userauth.util.CookieUtil.removeCookie;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

import com.tc.userauth.dto.AuthenticationRequestDto;
import com.tc.userauth.dto.AuthenticationResponseDto;
import com.tc.userauth.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDto> authenticate(@Valid @RequestBody final AuthenticationRequestDto requestDto) {
        final var authTokens = authenticationService.authenticate(requestDto.username(), requestDto.password());

        return ResponseEntity.ok()
                .header(SET_COOKIE, addCookie(REFRESH_TOKEN_COOKIE_NAME, authTokens.refreshToken(), authTokens.refreshTokenTtl()).toString())
                .body(new AuthenticationResponseDto(authTokens.accessToken()));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponseDto> refreshToken(@CookieValue(REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {
        final var authTokens = authenticationService.refreshToken(refreshToken);

        return ResponseEntity.ok(new AuthenticationResponseDto(authTokens.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> revokeToken(@CookieValue(REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {
        authenticationService.revokeRefreshToken(refreshToken);

        return ResponseEntity.noContent()
                .header(SET_COOKIE, removeCookie(REFRESH_TOKEN_COOKIE_NAME).toString())
                .build();
    }

}