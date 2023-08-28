package com.app.auth.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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

    @Column(length = 40)
    private String sessionId;

    @Email
    @NotNull
    @Column(unique = true, length = 100)
    private String email;

    @NotNull
    @Column(unique = true, length = 20, nullable = false)
    @Size(min = 3)
    private String username;

    @NotNull
    @Column(length = 90, nullable = false)
    @Size(min = 8)
    private String password;

    @Column(length = 16, unique = true)
    private String totpSecret;

    @Column(length = 40, unique = true)
    private String deviceId;

    @Lob
    private String privateKey;

    @Column(nullable = false)
    private Boolean isActive;

    private Date lastBiomAuth;

    public User(String email, String username, String password, String totpSecret, String deviceId, String rsaPrivateKey,
                Boolean isActive, Date lastBiometricAuthSuccess) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.totpSecret = totpSecret;
        this.deviceId = deviceId;
        this.privateKey = rsaPrivateKey;
        this.isActive = isActive;
        this.lastBiomAuth = lastBiometricAuthSuccess;
    }
}