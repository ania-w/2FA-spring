package com.ania.auth.controller;

import com.ania.auth.model.JwtToken;
import com.ania.auth.model.User;
import com.ania.auth.model.communication.request.CreateAccountRequest;
import com.ania.auth.model.communication.request.LoginRequest;
import com.ania.auth.repository.UserRepository;
import com.ania.auth.service.UserDetailsImpl;
import com.ania.auth.util.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;


    @GetMapping("/login")
    public ModelAndView loginViewPage (HttpServletResponse response) throws IOException {

        redirectIfUserAlreadyLoggedIn(response);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        return modelAndView;
    }

    private void redirectIfUserAlreadyLoggedIn(HttpServletResponse response) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated())
            response.sendRedirect("/api/content/index");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        JwtToken jwtToken = jwtUtils.generateJwtToken(userDetails);

        ResponseCookie tokenCookie = jwtUtils.generateJwtCookie(jwtToken);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString())
                .body(jwtToken.getJwtToken());
    }

    @PostMapping("/register-user")
    public ResponseEntity<?> registerUser(@Valid @RequestBody CreateAccountRequest request) {

        User user = new User(request.getUsername(), encoder.encode(request.getPassword()));

        System.out.println(encoder.encode(request.getPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("User " + request.getUsername() + " account created.");
    }


    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Cookie cookie = jwtUtils.clearJwtCookie();

        response.addCookie(cookie);

        response.sendRedirect("/api/auth/login");
    }
}