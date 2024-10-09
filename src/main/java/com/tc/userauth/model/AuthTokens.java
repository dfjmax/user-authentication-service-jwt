package com.tc.userauth.model;

import java.time.Duration;

public record AuthTokens(String accessToken, String refreshToken, Duration refreshTokenTtl) {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "RefreshToken";

}
