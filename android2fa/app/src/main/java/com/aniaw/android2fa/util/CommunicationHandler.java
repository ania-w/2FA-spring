package com.aniaw.android2fa.util;

import android.content.Context;
import android.se.omapi.Session;

import com.aniaw.android2fa.model.AuthAlias;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class CommunicationHandler {

    private static final String IP = "192.168.1.109:8081";
    private static final String GET_SESSION_ID_ENDPOINT = "/api/auth/android/session";
    private static final String SEND_AUTH_REQUEST_ENDPOINT = "/api/auth/android/session";

    private static String createSessionIdRequest(Context context) throws Exception {

        String username = KeystoreUtil.getUsername(context);
        String deviceId = KeystoreUtil.getSecret(context, AuthAlias.DEVICE_ID_ALIAS);

        String deviceIDWithTimestamp = deviceId + System.currentTimeMillis();

        String encryptedDeviceID = RSAEncryptionHelper.encrypt(context, deviceIDWithTimestamp);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        jsonObject.put("deviceId", encryptedDeviceID);

        return jsonObject.toString();
    }

    private static String createAuthRequest(Context context, String sessionId) throws Exception {
        String username = KeystoreUtil.getUsername(context);
        String deviceId = KeystoreUtil.getSecret(context, AuthAlias.DEVICE_ID_ALIAS);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("deviceId", deviceId);
        jsonObject.put("sessionId", sessionId);
        jsonObject.put("authenticationStatus", "success");

        String authenticationString = jsonObject.toString();
        String authenticationStringEncrypted = RSAEncryptionHelper.encrypt(context, authenticationString);

        jsonObject = new JSONObject();
        jsonObject.put("username", username);
        jsonObject.put("authenticationString", authenticationStringEncrypted);

        return jsonObject.toString();
    }


    public static void sendAuthResultToServer(Context context) {

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(() -> {

            try {
                String sessionId = getSessionId(context);
                sendAuthRequest(context, sessionId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        executorService.shutdown();

    }

    private static void sendAuthRequest(Context context, String sessionId) throws Exception {

        String body = createAuthRequest(context, sessionId);
        HttpsURLConnection connection = createConnection(SEND_AUTH_REQUEST_ENDPOINT, "POST");

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(body.getBytes());
        outputStream.flush();
        outputStream.close();

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Could not authenticate");
        }
    }


    private static String getSessionId(Context context) throws Exception {

        String body = createSessionIdRequest(context);

        HttpsURLConnection connection = createConnection(GET_SESSION_ID_ENDPOINT, "POST");

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(body.getBytes());
        outputStream.flush();
        outputStream.close();

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            return connection.getHeaderField("SessionId");
        }

        throw new RuntimeException("Could not retrieve session id");
    }

    private static HttpsURLConnection createConnection(String address, String method) throws Exception {
        URL requestUrl = new URL("https://" + IP + address);

        SSLContext sc = SSLContext.getInstance("TLS");
        TrustManager[] tmlist = {new MyTrustManager()};
        sc.init(null, tmlist, new java.security.SecureRandom());

        HostnameVerifier allowAllHostnames = (hostname, sslSession) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allowAllHostnames);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection connection = (HttpsURLConnection) requestUrl.openConnection();

        ((HttpsURLConnection) connection).setSSLSocketFactory(sc.getSocketFactory());
        connection.setRequestMethod(method);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        return connection;
    }

    private static class MyTrustManager implements X509TrustManager
    {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException
        {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException
        {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            return null;
        }

    }


}
