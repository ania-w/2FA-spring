package com.app.auth.model.communication.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateAccountResponse {
    byte[] qrCode;
}
