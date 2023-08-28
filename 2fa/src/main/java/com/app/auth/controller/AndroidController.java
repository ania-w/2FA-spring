package com.app.auth.controller;

import com.app.auth.model.communication.request.AndroidAuthRequest;
import com.app.auth.model.communication.request.GetSessionRequest;
import com.app.auth.service.UserService;
import com.app.auth.service.SessionService;
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
    SessionService sessionUtils;


    @PostMapping("/session")
    public ResponseEntity getSessionId(@RequestBody GetSessionRequest request){

        sessionUtils.verifySessionRequest(request);

        String sessionId = sessionUtils.generateSessionId(request.getUsername());

        return ResponseEntity.ok().header("SessionId", sessionId).build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity authenticate(@RequestBody AndroidAuthRequest request){

        sessionUtils.processAuthenticationRequest(request);

        return ResponseEntity.ok().build();
    }

}
