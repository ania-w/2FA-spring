package com.aniaw.android2fa.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class BiomericsUtil {

    private static Executor executor;
    Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());


    public static void authenticateUser(Context context, FragmentActivity fragmentActivity){

        executor = ContextCompat.getMainExecutor(context);


        BiometricPrompt biometricPrompt = new BiometricPrompt(fragmentActivity, executor, new BiometricPrompt.AuthenticationCallback() {

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                Toast.makeText(context, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();

                CommunicationHandler.sendAuthResultToServer(context);
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(context, "Authentication succeeded!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Authentication")
                .setSubtitle("Scan your fingerprint to authenticate")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);

    }



}
