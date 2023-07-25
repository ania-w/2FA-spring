package com.ania.auth.controller;

import com.ania.auth.model.communication.request.AndroidAuthRequest;
import com.ania.auth.model.communication.request.GetSessionRequest;
import com.ania.auth.service.UserService;
import com.ania.auth.util.SessionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/auth/android")
public class AndroidController {

    @Autowired
    UserService userService;

    @Autowired
    SessionUtil sessionUtils;


    @PostMapping("/session")
    public ResponseEntity getSessionId(@RequestBody GetSessionRequest request){

        sessionUtils.validateSessionRequest(request);

        String sessionId = sessionUtils.generateSessionId(request.getUsername());

        return ResponseEntity.ok().header("SessionId", sessionId).build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity authenticate(@RequestBody AndroidAuthRequest request){

        sessionUtils.processAuthenticationRequest(request);

        return ResponseEntity.ok().build();
    }

}
