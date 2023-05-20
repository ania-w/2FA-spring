package com.ania.auth.controller;

import com.ania.auth.model.JwtToken;
import com.ania.auth.model.TwoFactorMethod;
import com.ania.auth.model.User;
import com.ania.auth.model.communication.request.CreateAccountRequest;
import com.ania.auth.model.communication.request.LoginRequest;
import com.ania.auth.model.communication.request.OtpRequest;
import com.ania.auth.repository.UserRepository;
import com.ania.auth.util.JwtUtils;
import com.ania.auth.util.TotpUtils;
import jakarta.servlet.ServletException;
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

    @Autowired
    TotpUtils totpUtils;


    @GetMapping("/login")
    public ModelAndView loginViewPage (HttpServletResponse response,@RequestParam(required = false) String message) throws IOException {

        redirectIfUserAlreadyLoggedIn(response);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("message",message);

        return modelAndView;
    }

    @GetMapping("/get-otp")
    public Integer otp(String secretKey) {
        return totpUtils.getTotpPassword(secretKey);
    }

    @GetMapping("/get-users")
    public void users(String secret) {
        userRepository.findAll().forEach(System.out::println);
    }

    private void redirectIfUserAlreadyLoggedIn(HttpServletResponse response) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated())
            response.sendRedirect("/api/content/index");
    }

    private void redirectIfNotPreAuthenticated(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String jwtToken = jwtUtils.getJwtTokenFromRequest(request);
        if(!jwtUtils.isPreAuthenticated(jwtToken))
            response.sendRedirect("/api/auth/login");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        JwtToken jwtToken = jwtUtils.generatePreAuthJwtToken(username);

        ResponseCookie tokenCookie = jwtUtils.generateJwtCookie(jwtToken);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString()).body(jwtToken.getJwtToken());

    }

    @GetMapping("/otp")
    public ModelAndView otpViewPage() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("otp");

        return modelAndView;
    }

    @GetMapping("/second-factor")
    public void secondFactor(HttpServletRequest request, HttpServletResponse response) throws IOException {

        redirectIfUserAlreadyLoggedIn(response);

        redirectIfNotPreAuthenticated(request, response);

        String username = jwtUtils.getUserNameFromJwtToken(request);

        String twoFactorMethod = userRepository.findByUsername(username).get().getTwoFactorMethod();

        switch (twoFactorMethod){
            case TwoFactorMethod.OTP ->  response.sendRedirect("/api/auth/otp");
            case TwoFactorMethod.BIOMETRICS -> response.sendRedirect("/api/auth/biometrics");
            case TwoFactorMethod.NONE -> response.sendRedirect("/api/content/index");
        }
    }


    @PostMapping("/otp")
    private ResponseEntity<?> authenticateWithOTP(HttpServletRequest request, HttpServletResponse response, @Valid @RequestBody OtpRequest otpRequest) throws IOException {

        redirectIfUserAlreadyLoggedIn(response);
        redirectIfNotPreAuthenticated(request, response);

        String username = jwtUtils.getUserNameFromJwtToken(request);

        String secret = userRepository.findByUsername(username).get().getSecret();

        totpUtils.validateTotp(secret, otpRequest.getOtp());

        JwtToken jwtToken = jwtUtils.generateJwtToken(username);

        ResponseCookie tokenCookie = jwtUtils.generateJwtCookie(jwtToken);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString()).body(jwtToken.getJwtToken());
    }

    @GetMapping("/register-user")
    public ModelAndView registerUserViewPage (HttpServletResponse response) throws IOException {

        redirectIfUserAlreadyLoggedIn(response); // TODO: maybe logout?

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register_user");

        return modelAndView;
    }

    @PostMapping("/register-user")
    public ResponseEntity<?> registerUser(@Valid @RequestBody CreateAccountRequest request) {

        String secret =totpUtils.getKey();

        String twoFactorMethod = TwoFactorMethod.OTP;//TwoFactorMethod.valueOf(request.getTwoFactorMethod());

        User user = new User(request.getUsername(), encoder.encode(request.getPassword()), request.getEmail(), secret, twoFactorMethod);

        userRepository.save(user);

        return ResponseEntity.ok("User " + request.getUsername() + " account created with secret: " + secret);
    }


    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Cookie cookie = jwtUtils.clearJwtCookie();

        response.addCookie(cookie);

        response.sendRedirect("/api/auth/login");
    }

}