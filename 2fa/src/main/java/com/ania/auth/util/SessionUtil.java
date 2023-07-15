package com.ania.auth.util;

import com.ania.auth.model.User;
import com.ania.auth.model.communication.request.GetSessionRequest;
import com.ania.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class SessionUtil {

    @Autowired
    UserRepository userRepository;

    public void validateSessionRequest(GetSessionRequest sessionRequest) {

        if(userRepository.findByUsername(sessionRequest.getUsername()).isPresent()) {
            User user = userRepository.findByUsername(sessionRequest.getUsername()).get();

            String deviceId = decryptDeviceId(sessionRequest.getDeviceId(), user.getRsaPrivateKey());

            deviceId = deviceId.substring(0,deviceId.length() - 13);

            if(!deviceId.equals(user.getDeviceId()))
                throw new RuntimeException("Invalid Session Request");

        } else {
            throw new RuntimeException("Invalid Session Request");
        }

    }

    private String decryptDeviceId(String encryptedDeviceId, String rsaPrivateKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] rsaKeyBytes = Base64.getDecoder().decode(rsaPrivateKey);
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(rsaKeyBytes));

            byte[] decodedDeviceId = Base64.getDecoder().decode(encryptedDeviceId.getBytes(StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(cipher.doFinal(decodedDeviceId));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String generateSessionId(String username) {
        String random = UUID.randomUUID().toString();
        String timestamp = LocalDateTime.now().toString();
        String sessionId = random+"_"+username + "_" + timestamp;

        return Base64.getEncoder().encodeToString(sessionId.getBytes(StandardCharsets.UTF_8));
    }

}
