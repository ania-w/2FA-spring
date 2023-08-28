package com.aniaw.android2fa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.aniaw.android2fa.databinding.FragmentRegisterBinding;
import com.aniaw.android2fa.model.AuthAlias;
import com.aniaw.android2fa.model.RegisterData;
import com.aniaw.android2fa.util.KeystoreUtil;
import com.google.gson.Gson;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import java.nio.charset.StandardCharsets;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;

    private final Gson gson = new Gson();

    private ActivityResultLauncher<Intent> qrCodeScanLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.qrPicture.setOnClickListener(this::scan);
        binding.back.setOnClickListener(this::back);
        binding.submitSecret.setOnClickListener(this::saveSecretViaTextView);

        setupQRCodeLauncher();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void saveSecretViaTextView(View view) {

    try{
        String content = binding.secretTextView.getText().toString();

        byte[] decoded = Base64.decode(content, Base64.DEFAULT);

        String userAuthDataString = new String(decoded, StandardCharsets.UTF_8);

        RegisterData registerData = gson.fromJson(userAuthDataString, RegisterData.class);

        KeystoreUtil.saveUsername(this.getContext(), registerData.getUsername());
        KeystoreUtil.setKeyStoreSecret(this.getContext(),registerData.getTotpSecret(), AuthAlias.OTP_ALIAS);
        KeystoreUtil.setKeyStoreSecret(this.getContext(),registerData.getRsaSecret(), AuthAlias.DEVICE_ID_ALIAS);
        KeystoreUtil.setKeyStoreSecret(this.getContext(),registerData.getDeviceId(), AuthAlias.RSA_SECRET_ALIAS);

    } catch (Exception e){
            Toast.makeText(getContext()," Couldn't save secret. Please try again.", Toast.LENGTH_SHORT).show();
    }

        back(view);

    }

    private void setupQRCodeLauncher() {
        qrCodeScanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        try {
                            processQRCodeScanResult(data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Scan cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }


    public void scan(View v) {
        ScanOptions scanOptions = new ScanOptions();
        ScanContract scanContract = new ScanContract();
        scanOptions.setPrompt("Scan QR Code");
        scanOptions.setOrientationLocked(false);
        Intent intent = scanContract.createIntent(getContext(),scanOptions);
        qrCodeScanLauncher.launch(intent);
    }

    public void back(View v) {
        NavHostFragment.findNavController(RegisterFragment.this)
                .navigate(R.id.action_registerFragment_to_loginFragment);
    }

    private void processQRCodeScanResult(Intent data) throws Exception {

        ScanContract scanContract=new ScanContract();
        ScanIntentResult result = scanContract.parseResult(Activity.RESULT_OK, data);

        String qrCodeContent = result.getContents();

        RegisterData registerData = gson.fromJson(qrCodeContent, RegisterData.class);
        KeystoreUtil.saveUsername(this.getContext(), registerData.getUsername());

        KeystoreUtil.setKeyStoreSecret(this.getContext(),registerData.getTotpSecret(), AuthAlias.OTP_ALIAS);
        KeystoreUtil.setKeyStoreSecret(this.getContext(),registerData.getRsaSecret(), AuthAlias.RSA_SECRET_ALIAS);
        KeystoreUtil.setKeyStoreSecret(this.getContext(),registerData.getDeviceId(), AuthAlias.DEVICE_ID_ALIAS);

        NavHostFragment.findNavController(RegisterFragment.this)
                .navigate(R.id.action_registerFragment_to_loginFragment);

        Toast.makeText(super.getContext(),"Secret saved successfully", Toast.LENGTH_SHORT).show();
    }

}