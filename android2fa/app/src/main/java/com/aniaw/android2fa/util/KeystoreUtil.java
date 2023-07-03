package com.aniaw.android2fa.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class KeystoreUtil {
    private static final String KEY_ALIAS = "totp_secret";
    private static final String PREFERENCES = "shared_preferences";
    private static final String IV_ALIAS = "IV";
    private static final String CHARSET_NAME = StandardCharsets.UTF_8.name();

    public static void saveKey(Context context, String secret) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        }

        String encryptedSecret = encrypt(context, secret);
        saveEncryptedSecret(context,encryptedSecret);

    }


    public static String getKey(Context context) throws Exception {
        String secret = getEncryptedSecret(context);
        return decrypt(context,secret);
    }

    private static Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
    }


    private static void saveEncryptedSecret(Context context, String encryptedSecret) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit().putString(KEY_ALIAS, encryptedSecret).apply();
    }

    private static String getEncryptedSecret(Context context) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getString(KEY_ALIAS, "");
    }

    private static String encrypt(Context context, String secret) throws Exception {
        Cipher cipher = getCipher();
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), cipher.getParameters());
        byte[] iv = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
        String ivString = Base64.encodeToString(iv, Base64.DEFAULT);
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
                .putString(IV_ALIAS, ivString).apply();
        byte[] encryptedData = cipher.doFinal(secret.getBytes(CHARSET_NAME));
        return Base64.encodeToString(encryptedData, Base64.DEFAULT);
    }

    private static String decrypt(Context context, String encryptedSecret) throws Exception {
        Cipher cipher = getCipher();
        String ivString = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getString(IV_ALIAS, "");
        byte[] iv = Base64.decode(ivString, Base64.DEFAULT);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), new IvParameterSpec(iv));
        byte[] encryptedData = Base64.decode(encryptedSecret, Base64.DEFAULT);
        byte[] decryptedData = cipher.doFinal(encryptedData);
        return new String(decryptedData, CHARSET_NAME);
    }

    private static SecretKey getSecretKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        return (SecretKey) keyStore.getKey(KEY_ALIAS, null);
    }

}
