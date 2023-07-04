package com.aniaw.android2fa;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.aniaw.android2fa.databinding.FragmentFirstBinding;
import com.aniaw.android2fa.util.KeystoreUtil;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        binding.newConfig.setOnClickListener(this::config);
        binding.modifyConfig2.setOnClickListener(this::config);

        super.onViewCreated(view, savedInstanceState);

        if(KeystoreUtil.isUserRegistered()){
            prepareAuthenticationView();
        } else {
            prepareNewAccountView();
        }

   }

    private void prepareNewAccountView() {
        binding.newAccountText.setVisibility(View.VISIBLE);
        binding.newConfig.setVisibility(View.VISIBLE);
    }

    private void prepareAuthenticationView() {
        binding.fingerprint.setVisibility(View.VISIBLE);
        binding.infoText.setVisibility(View.VISIBLE);
        binding.modifyConfig2.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.OTP.setVisibility(View.VISIBLE);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void config(View v) {
        NavHostFragment.findNavController(FirstFragment.this)
                .navigate(R.id.action_FirstFragment_to_registerFragment);
    }



}