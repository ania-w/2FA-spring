package com.ania.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtToken {
    String jwtToken;
    String username;
    String expirationTime;
}
