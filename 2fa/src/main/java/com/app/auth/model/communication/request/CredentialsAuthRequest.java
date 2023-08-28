package com.app.auth.model.communication.request;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CredentialsAuthRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

}
