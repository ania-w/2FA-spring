package com.ania.auth.util;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

public final class TotpUtils {

    private static final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public static void validateTotp(String secretKey, Integer otp) {
        boolean isCodeValid = gAuth.authorize(secretKey, otp);

        if (!isCodeValid) {
            SecurityContextHolder.clearContext();
            throw new BadCredentialsException("Invalid OTP");
        }
    }

    public static Integer getTotpPassword(String secretKey) {
        return gAuth.getTotpPassword(secretKey);
    }

    public static String getKey() {
        final GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }
}
