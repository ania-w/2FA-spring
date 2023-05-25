package com.aniaw.android2fa;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
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

import com.aniaw.android2fa.config.Util;
import com.aniaw.android2fa.databinding.FragmentRegisterBinding;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;

    private ActivityResultLauncher<Intent> qrCodeScanLauncher;

    private KeyStore keyStore;

    private static final String KEY_ALIAS = "totpSecret";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        initializeKeyStore();
        return binding.getRoot();

    }

    private void initializeKeyStore() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

                KeyGenerator keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES,
                        "AndroidKeyStore"
                );

                KeyGenParameterSpec.Builder builder = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    builder = new KeyGenParameterSpec.Builder(
                            KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
                    )
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);
                }

                assert builder != null;

                keyGenerator.init(builder.build());
                keyGenerator.generateKey();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.qrPicture.setOnClickListener(this::onClick);

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void onClick(View v) {
        ScanOptions scanOptions = new ScanOptions();
        ScanContract scanContract = new ScanContract();
        scanOptions.setPrompt("Scan QR Code");
        scanOptions.setOrientationLocked(false);
        Intent intent = scanContract.createIntent(this.getContext(),scanOptions);
        qrCodeScanLauncher.launch(intent);
    }

    private void processQRCodeScanResult(Intent data) throws Exception {

        ScanContract scanContract=new ScanContract();
        ScanIntentResult result = scanContract.parseResult(Activity.RESULT_OK, data);

        String secret = result.getContents();
        initializeKeyStore();
        saveSecret(secret);

    }

    private void saveSecret(String secret) throws Exception {

        KeyStore.SecretKeyEntry secretKeyEntry =
                (KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null);

        SecretKey secretKey = secretKeyEntry.getSecretKey();
        byte[] encryptedData = encryptData(secretKey, secret);
        String encryptedSecret = Base64.encodeToString(encryptedData, Base64.DEFAULT);

        Util.setProperty(KEY_ALIAS, encryptedSecret, getContext());

        NavHostFragment.findNavController(RegisterFragment.this)
                .navigate(R.id.action_registerFragment_to_FirstFragment);

        Toast.makeText(super.getContext(),"Secret saved successfully", Toast.LENGTH_SHORT).show();
    }


    private byte[] encryptData(SecretKey secretKey, String data) throws Exception {
        Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        return cipher.doFinal(data.getBytes());
    }

    private String decryptData(SecretKey secretKey, byte[] encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);

        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] decryptedBytes = cipher.doFinal(encryptedData);

        return new String(decryptedBytes);
    }

}