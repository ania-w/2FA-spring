package com.aniaw.android2fa.util;

import static com.aniaw.android2fa.model.AuthAlias.*;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.aniaw.android2fa.model.AuthAlias;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class KeystoreUtil {

    private static final String PREFERENCES = "shared_preferences";
    private static final String CHARSET_NAME = StandardCharsets.UTF_8.name();

    public static void saveUsername(Context context,String username){
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit().putString("USERNAME", username).apply();
    }

    public static String getUsername(Context context){
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getString("USERNAME", "");
    }

    public static void setKeyStoreSecret(Context context, String secret, AuthAlias alias) throws Exception {
        saveKey(context,secret,alias);
    }

    private static SecretKey getKeystoreSecret(AuthAlias alias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        return (SecretKey) keyStore.getKey(alias.name(), null);
    }

    private static void saveKey(Context context, String secret, AuthAlias alias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        if (!keyStore.containsAlias(alias.name())) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(new KeyGenParameterSpec.Builder(
                    alias.name(),
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        }

        String encryptedSecret = encrypt(context, secret, alias);
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit().putString(alias.name(), encryptedSecret).apply();

    }

    public static Boolean isUserRegistered(){
        try{
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            return keyStore.containsAlias(OTP_ALIAS.name());
        } catch (Exception e){
            return false;
        }
    }

    public static String getSecret(Context context, AuthAlias alias) throws Exception {
        String secret = getEncryptedSecret(context, alias);
        return decrypt(context,secret, alias);
    }

    private static Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
    }


    private static String getEncryptedSecret(Context context, AuthAlias alias) {
        return context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getString(alias.name(), "");
    }

    private static String encrypt(Context context, String secret, AuthAlias alias) throws Exception {
        Cipher cipher = getCipher();
        cipher.init(Cipher.ENCRYPT_MODE, getKeystoreSecret(alias), cipher.getParameters());
        byte[] iv = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
        String ivString = Base64.encodeToString(iv, Base64.DEFAULT);
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
                .putString(IV_ALIAS.name()+"."+alias, ivString).apply();

        byte[] encryptedData = cipher.doFinal(secret.getBytes(CHARSET_NAME));
        String base64 =  Base64.encodeToString(encryptedData, Base64.DEFAULT);
        return base64;
    }

    private static String decrypt(Context context, String encryptedSecret, AuthAlias alias) throws Exception {
        Cipher cipher = getCipher();
        String ivString = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getString(IV_ALIAS.name()+"."+alias, "");
        byte[] iv = Base64.decode(ivString, Base64.DEFAULT);
        cipher.init(Cipher.DECRYPT_MODE, getKeystoreSecret(alias), new IvParameterSpec(iv));
        byte[] encryptedData = Base64.decode(encryptedSecret, Base64.DEFAULT);
        String encrypted =  new String(encryptedData, CHARSET_NAME);

        byte[] decryptedData = cipher.doFinal(encryptedData);

        String decrypted =  new String(decryptedData, CHARSET_NAME);

        return decrypted;
    }

}
