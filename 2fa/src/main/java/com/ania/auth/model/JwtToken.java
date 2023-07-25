package com.ania.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class JwtToken {
    String jwtToken;
    String username;
    Date expirationTime;
}
