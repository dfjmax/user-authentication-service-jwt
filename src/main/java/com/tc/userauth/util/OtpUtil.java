package com.tc.userauth.util;

import static java.util.stream.IntStream.range;

import java.security.SecureRandom;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OtpUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String generateOtp(Integer length) {
        StringBuilder otp = new StringBuilder();

        range(0, length).mapToObj(i -> SECURE_RANDOM.nextInt(10)).forEach(otp::append);

        return otp.toString();
    }

}
