package com.aniaw.android2fa;

import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.aniaw.android2fa.databinding.FragmentLoginBinding;
import com.aniaw.android2fa.model.AuthAlias;
import com.aniaw.android2fa.util.BiomericsUtil;
import com.aniaw.android2fa.util.KeystoreUtil;
import com.aniaw.android2fa.util.OtpUtil;

public class LoginFragment extends Fragment {

    private static final int COUNTDOWN_DURATION = 30000;
    private static final int COUNTDOWN_STEP = 100;
    private FragmentLoginBinding binding;


    private static Handler handler;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    private void authenticate() {
        BiomericsUtil.authenticateUser(getContext(),getActivity());
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        binding.newConfig.setOnClickListener(this::config);
        binding.modifyConfig2.setOnClickListener(this::config);
        binding.scanFingerprint.setOnClickListener(this::scanFingerprint);
        
        super.onViewCreated(view, savedInstanceState);

        if(KeystoreUtil.isUserRegistered()){
            try {
                prepareAuthenticationView();
                generateOTP();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
                prepareNewAccountView();
        }

   }

    private void scanFingerprint(View view) {

        BiomericsUtil.authenticateUser(getContext(), getActivity());

    }


    private void prepareNewAccountView()  {
        binding.newAccountText.setVisibility(View.VISIBLE);
        binding.newConfig.setVisibility(View.VISIBLE);
    }

    private void generateOTP() throws Exception {
        String secret = KeystoreUtil.getSecret(getContext(), AuthAlias.OTP_ALIAS);

        String otp = OtpUtil.generateOtp(secret);

        binding.OTP.setText(otp);

    }

    private void prepareAuthenticationView() {
        binding.fingerprint.setVisibility(View.VISIBLE);
        binding.infoText.setVisibility(View.VISIBLE);
        binding.modifyConfig2.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.OTP.setVisibility(View.VISIBLE);
        binding.scanFingerprint.setVisibility(View.VISIBLE);
        binding.progressBar.setMax(COUNTDOWN_DURATION / COUNTDOWN_STEP);

        new CountDownTimer(COUNTDOWN_DURATION, COUNTDOWN_STEP) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(binding!=null)
                    binding.progressBar.setProgress((int) (millisUntilFinished / COUNTDOWN_STEP));
            }

            @Override
            public void onFinish() {
                try {
                    generateOTP();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.start();
            }
        }.start();

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void config(View v) {
        NavHostFragment.findNavController(LoginFragment.this)
                .navigate(R.id.action_LoginFragment_to_registerFragment);
    }



}