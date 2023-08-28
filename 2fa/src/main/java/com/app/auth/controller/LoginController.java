package com.app.auth.controller;

import com.app.auth.model.JwtToken;
import com.app.auth.model.Roles;
import com.app.auth.model.User;
import com.app.auth.model.communication.request.CredentialsAuthRequest;
import com.app.auth.model.communication.request.OtpRequest;
import com.app.auth.model.communication.response.CreateAccountResponse;
import com.app.auth.model.communication.response.User2faData;
import com.app.auth.service.UserService;
import com.app.auth.service.util.JwtUtils;
import com.app.auth.service.util.QrCodeUtil;
import com.app.auth.service.util.TotpUtils;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LoginController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserService userService;

    @Autowired
    JwtUtils jwtUtils;


    @GetMapping("/login")
    public ModelAndView loginViewPage (HttpServletResponse response,@RequestParam(required = false) String message) throws IOException {

        redirectIfUserAlreadyLoggedIn(response);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("message",message);

        return modelAndView;
    }

    @GetMapping("/get-users")
    public void users(String secret) {
        userService.printAllUsers();
    }



    @PostMapping("/login")
    public ResponseEntity<?> firstFactorAuthentication(@Valid @RequestBody CredentialsAuthRequest request) {

        String username = request.getUsername();
        String password = request.getPassword();

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        User user = userService.findUserByUsername(username);

        if(!user.getIsActive()) {
            JwtToken jwtToken = jwtUtils.generateTempJwtToken(username, Roles.PRE_REGISTERED);

            ResponseCookie tokenCookie = jwtUtils.generateJwtCookie(jwtToken);
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString()).body("activate");
        }
        JwtToken jwtToken = jwtUtils.generateTempJwtToken(username, Roles.PRE_AUTHENTICATED);

        ResponseCookie tokenCookie = jwtUtils.generateJwtCookie(jwtToken);


        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString()).body(jwtToken.getJwtToken());
    }

    @GetMapping("/second-factor")
    public ModelAndView secondFactorAuthentication(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ModelAndView modelAndView = new ModelAndView();

        if(!redirectIfUserAlreadyLoggedIn(response) && !redirectIfNotAuthorized(request, response, Roles.PRE_AUTHENTICATED)) {

            modelAndView.setViewName("second_factor");
        } else {
            modelAndView.setViewName("login");

        }
        return modelAndView;
    }


    @PostMapping("/otp")
    private ResponseEntity<?> authenticateWithOTP(HttpServletRequest request, HttpServletResponse response, @Valid @RequestBody OtpRequest otpRequest) throws IOException {

        if(!redirectIfUserAlreadyLoggedIn(response) && !redirectIfNotAuthorized(request, response, Roles.PRE_AUTHENTICATED)) {

            String username = jwtUtils.getUserNameFromJwtToken(request);

            String secret = userService.findUserByUsername(username).getTotpSecret();

            TotpUtils.validateTotp(secret, otpRequest.getOtp());

            JwtToken jwtToken = jwtUtils.generateJwtToken(username);

            ResponseCookie tokenCookie = jwtUtils.generateJwtCookie(jwtToken);

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString()).body(jwtToken.getJwtToken());
        }
        return ResponseEntity.badRequest().build();
    }



    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Cookie cookie = jwtUtils.clearJwtCookie();

        response.addCookie(cookie);

        response.sendRedirect("/api/auth/login");
    }

    @GetMapping("/auth-biometric")
    public ResponseEntity validateBiometricAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if(redirectIfNotAuthorized(request, response, Roles.PRE_AUTHENTICATED))
            return ResponseEntity.badRequest().build();

        JwtToken token = jwtUtils.getJwtFromCookies(request).get();

        User user = userService.findUserByUsername(token.getUsername());

        Date date = user.getLastBiomAuth();

        if(date!=null) {
            long fiveMinutesInMillis = 5 * 60 * 1000;
            Date fiveMinutesEarlier = new Date(token.getExpirationTime().getTime() - fiveMinutesInMillis);

            if (date.after(fiveMinutesEarlier) && date.before(token.getExpirationTime())) {
                JwtToken jwtToken = jwtUtils.generateJwtToken(user.getUsername());

                ResponseCookie tokenCookie = jwtUtils.generateJwtCookie(jwtToken);

                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString()).body(jwtToken.getJwtToken());
            }
        }
        return ResponseEntity.accepted().build();
    }

    public Boolean redirectIfUserAlreadyLoggedIn(HttpServletResponse response) throws IOException {
        if(isLoggedIn()) {
            response.sendRedirect("/api/content/index");
            return true;
        }
        return false;
    }

    private boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    public Boolean redirectIfNotAuthorized(HttpServletRequest request, HttpServletResponse response, Roles role) throws IOException {
        if (isAuthorized(request, role)) {
            return false;
        } else {
            response.sendRedirect("/api/auth/login");
            return true;
        }
    }

    private boolean isAuthorized(HttpServletRequest request, Roles role) {
        if(jwtUtils.isJwtTokenPresent(request)) {
            String jwtToken = jwtUtils.getJwtTokenFromRequest(request);
            return jwtUtils.validateJwtToken(jwtToken) && jwtUtils.hasRole(jwtToken, role);
        } else {
            return false;
        }
    }


}