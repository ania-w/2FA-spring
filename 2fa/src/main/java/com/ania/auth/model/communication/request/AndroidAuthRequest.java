package com.ania.auth.model.communication.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AndroidAuthRequest {

    String username;
    String authenticationString;
}
