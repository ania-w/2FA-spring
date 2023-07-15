package com.ania.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
        })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String username;

    private String password;

    private String totpSecret;

    private String deviceId;

    @Lob
    private String rsaPrivateKey;

    private Boolean twoFactorEnabled;

    private Date lastBiometricAuthSuccess;

    public User(String email, String username, String password, String totpSecret, String deviceId, String rsaPrivateKey,
                Boolean twoFactorEnabled, Date lastBiometricAuthSuccess) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.totpSecret = totpSecret;
        this.deviceId = deviceId;
        this.rsaPrivateKey = rsaPrivateKey;
        this.twoFactorEnabled = twoFactorEnabled;
        this.lastBiometricAuthSuccess = lastBiometricAuthSuccess;
    }
}