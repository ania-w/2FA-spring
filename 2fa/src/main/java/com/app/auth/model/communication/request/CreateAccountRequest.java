package com.app.auth.model.communication.request;

import jakarta.annotation.Nullable;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CreateAccountRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    @Size(min = 5, max = 30)
    private String password;

    @Nullable
    @Email
    @Size(min = 5, max = 100)
    private String email;
}
