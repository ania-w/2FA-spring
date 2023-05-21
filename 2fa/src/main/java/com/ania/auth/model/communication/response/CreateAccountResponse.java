package com.ania.auth.model.communication.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateAccountResponse {
    byte[] qrCode;
}
