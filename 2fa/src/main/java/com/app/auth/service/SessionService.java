package com.app.auth.service;

import com.app.auth.model.User;
import com.app.auth.model.communication.request.AndroidAuthRequest;
import com.app.auth.model.communication.request.GetSessionRequest;
import com.app.auth.repository.UserRepository;
import com.app.auth.model.communication.AuthenticationString;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class SessionService {

    @Autowired
    UserRepository userRepository;

    private static Gson gson = new Gson();

    public void verifySessionRequest(GetSessionRequest sessionRequest) {

        if(userRepository.findByUsername(sessionRequest.getUsername()).isPresent()) {
            User user = userRepository.findByUsername(sessionRequest.getUsername()).get();

            String deviceId = decrypt(sessionRequest.getDeviceId(), user.getPrivateKey());

            deviceId = deviceId.substring(0,deviceId.length() - 13);

            if(!deviceId.equals(user.getDeviceId()))
                throw new RuntimeException("Invalid Session Request");

        } else {
            throw new RuntimeException("Invalid Session Request");
        }

    }

    public static String decrypt(String message, String rsaPrivateKey) {
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
        User user = userRepository.findByUsername(username).get();

        saveSessionIdInDb(user, random);

        return Base64.getEncoder().encodeToString(random.getBytes(StandardCharsets.UTF_8));
    }

    public void processAuthenticationRequest(AndroidAuthRequest request) {

        if(userRepository.findByUsername(request.getUsername()).isPresent()) {
            User user = userRepository.findByUsername(request.getUsername()).get();
            String sessionId = user.getSessionId();
            if(sessionId!=null) {
                String authString = decrypt(request.getAuthenticationString(), user.getPrivateKey());

                AuthenticationString authenticationString = gson.fromJson(authString, AuthenticationString.class);
                String decodedSessionId = new String(Base64.getDecoder().decode(authenticationString.getSessionId().getBytes()));


                if (!authenticationString.getDeviceId().equals(user.getDeviceId()) &&
                        !authenticationString.getSessionId().equals(sessionId))
                    throw new RuntimeException("Invalid request");

                saveSessionIdInDb(user, null);
                setLastAuthenticated(user);
            }
        } else {
            throw new RuntimeException("Invalid request");
        }
    }

    private void setLastAuthenticated(User user) {
        LocalDateTime localDateTime = LocalDateTime.now();
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        user.setLastBiomAuth(date);
            userRepository.save(user);
    }

    public void saveSessionIdInDb(User user, String sessionId) {
        user.setSessionId(sessionId);
        userRepository.save(user);
    }
}
