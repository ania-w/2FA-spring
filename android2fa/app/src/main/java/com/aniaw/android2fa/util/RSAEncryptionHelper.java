package com.aniaw.android2fa.util;

import android.content.Context;
import android.util.Base64;

import com.aniaw.android2fa.model.AuthAlias;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RSAEncryptionHelper {
    public static String encrypt(Context context, String message) {
        try {
            String publicKeyString = KeystoreUtil.getSecret(context, AuthAlias.RSA_SECRET_ALIAS);
            byte[] publicKeyByte = Base64.decode(publicKeyString, Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyByte);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] encryptedBytes = cipher.doFinal(message.getBytes());
            return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}