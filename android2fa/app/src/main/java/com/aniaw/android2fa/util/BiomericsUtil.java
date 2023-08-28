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


    public static void authenticateUser(Context context, FragmentActivity fragmentActivity){

        executor = ContextCompat.getMainExecutor(context);


        BiometricPrompt biometricPrompt = new BiometricPrompt(fragmentActivity, executor, new BiometricPrompt.AuthenticationCallback() {

            @Override
            public void onAuthenticationError(int errorCode, CharSequence err) {
                Toast.makeText(context, "Authentication error: " + err, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                CommunicationHandler.sendAuthResultToServer(context);
                Toast.makeText(context, "Authentication successful!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onAuthenticationFailed() {
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Scan your fingerprint to authenticate")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);

    }



}
