package com.aniaw.android2fa.util;

import org.jboss.aerogear.security.otp.Totp;

public class OtpUtil {

    public static String generateOtp(String secret){
            Totp totp = new Totp(secret);
            return totp.now();
    }

}
