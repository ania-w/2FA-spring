package com.app.auth.service.util;

import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;

public final class TotpUtils {

    public static void validateTotp(String secretKey, Integer otp) {
        Totp totp = new Totp(secretKey);

        boolean isCodeValid = totp.verify(otp.toString());

        if (!isCodeValid) {
            SecurityContextHolder.clearContext();
            throw new BadCredentialsException("Invalid OTP");
        }
    }

    public static String generateSecret() {
        return Base32.random();
    }

    public static String generateOtp(String secretKey) {
        return new Totp(secretKey).now();
    }
}
