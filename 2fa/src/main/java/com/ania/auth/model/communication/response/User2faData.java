package com.ania.auth.model.communication.response;

import com.ania.auth.util.TotpUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

@Data
@AllArgsConstructor
public class User2faData {
    String totpSecret;
    String deviceId;
    KeyPair keyPair;
    String username;

    public User2faData(String username) {
        this.username = username;
        totpSecret = TotpUtils.generateSecret();
        deviceId = UUID.randomUUID().toString();
        keyPair = generateRSASecret();
        System.out.println(Arrays.toString(keyPair.getPrivate().getEncoded()));
    }

    private static KeyPair generateRSASecret() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "{" +
                "'totpSecret':'" + totpSecret + '\'' +
                ", 'deviceId':'" + deviceId + '\'' +
                ", 'rsaSecret':'" + Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()) + '\'' +
                ", 'username':'" + username + '\'' +
                '}';
    }
}
