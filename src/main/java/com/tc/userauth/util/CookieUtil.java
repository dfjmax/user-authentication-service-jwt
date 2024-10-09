package com.tc.userauth.util;

import java.time.Duration;
import org.springframework.http.ResponseCookie;

public class CookieUtil {

    public static ResponseCookie addCookie(String name, String value, Duration duration) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .path("/")
                .maxAge(duration)
                .build();
    }

    public static ResponseCookie removeCookie(String name) {
        return ResponseCookie.from(name)
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
    }

}
