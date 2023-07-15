package com.aniaw.android2fa.model;

public class RegisterData {
    String username;
    String totpSecret;
    String deviceId;
    String rsaSecret;

    public RegisterData(String username, String totpSecret, String deviceId, String rsaSecret) {
        this.username = username;
        this.totpSecret = totpSecret;
        this.deviceId = deviceId;
        this.rsaSecret = rsaSecret;
    }

    public RegisterData() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTotpSecret() {
        return totpSecret;
    }

    public void setTotpSecret(String otpSecret) {
        this.totpSecret = otpSecret;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getRsaSecret() {
        return rsaSecret;
    }

    public void setRsaSecret(String rsaSecret) {
        this.rsaSecret = rsaSecret;
    }
}
