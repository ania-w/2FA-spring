package com.ania.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
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

    private String secret;

    private String twoFactorMethod;

    private Boolean twoFactorEnabled;

    public User(String username, String password, String email, String secret, String twoFactorMethod, Boolean twoFactorEnabled) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.secret = secret;
        this.twoFactorMethod = twoFactorMethod;
        this.twoFactorEnabled = twoFactorEnabled;
    }
}