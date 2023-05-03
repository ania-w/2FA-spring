package com.ania.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

//TODO: refresh token implementation
@Data
@AllArgsConstructor
public class JwtToken {
    String jwtToken;
    String username;
    String expirationTime;
}
