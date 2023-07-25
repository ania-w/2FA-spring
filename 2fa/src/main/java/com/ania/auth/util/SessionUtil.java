package com.ania.auth.util;

import com.ania.auth.model.User;
import com.ania.auth.model.communication.AuthenticationString;
import com.ania.auth.model.communication.request.AndroidAuthRequest;
import com.ania.auth.model.communication.request.GetSessionRequest;
import com.ania.auth.repository.UserRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class SessionUtil {

    @Autowired
    UserRepository userRepository;

    private static Gson gson = new Gson();

    public void validateSessionRequest(GetSessionRequest sessionRequest) {

        if(userRepository.findByUsername(sessionRequest.getUsername()).isPresent()) {
            User user = userRepository.findByUsername(sessionRequest.getUsername()).get();

            String deviceId = decrypt(sessionRequest.getDeviceId(), user.getRsaPrivateKey());

            deviceId = deviceId.substring(0,deviceId.length() - 13);

            if(!deviceId.equals(user.getDeviceId()))
                throw new RuntimeException("Invalid Session Request");

        } else {
            throw new RuntimeException("Invalid Session Request");
        }

    }

    private String decrypt(String message, String rsaPrivateKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] rsaKeyBytes = Base64.getDecoder().decode(rsaPrivateKey);
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(rsaKeyBytes));

            byte[] decoded= Base64.getDecoder().decode(message.getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(cipher.doFinal(decoded));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String generateSessionId(String username) {
        String random = UUID.randomUUID().toString();
        String timestamp = LocalDateTime.now().toString();
        String sessionId = random + "_"+username + "_" + timestamp;

        return Base64.getEncoder().encodeToString(sessionId.getBytes(StandardCharsets.UTF_8));
    }

    public void processAuthenticationRequest(AndroidAuthRequest request) {

        if(userRepository.findByUsername(request.getUsername()).isPresent()) {
            User user = userRepository.findByUsername(request.getUsername()).get();

            String authString = decrypt(request.getAuthenticationString(), user.getRsaPrivateKey());

            AuthenticationString authenticationString = gson.fromJson(authString, AuthenticationString.class);
            String decodedSessionId = new String(Base64.getDecoder().decode(authenticationString.getSessionId().getBytes()));

            if(!authenticationString.getDeviceId().equals(user.getDeviceId()))
                    throw new RuntimeException("Invalid Authentication Request");

            setLastAuthenticated(user, decodedSessionId);
        } else {
            throw new RuntimeException("Invalid Authentication Request");
        }

    }

    private void setLastAuthenticated(User user, String sessionId) {

        String[] parts = sessionId.split("_");

        String username = parts[1];
        String date = parts[2];

        if(user.getUsername().equals(username)) {
            user.setLastBiometricAuthSuccess(date);
            userRepository.save(user);
        }
    }
}
