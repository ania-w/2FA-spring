package com.app.auth.model.communication.request;

import lombok.Data;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;

@Data
public class OtpRequest {

    @NotBlank
    @Digits(integer = 6, fraction = 0)
    Integer otp;
}
