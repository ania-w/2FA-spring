package com.ania.auth.controller;

import com.ania.auth.model.JwtToken;
import com.ania.auth.model.Roles;
import com.ania.auth.model.User;
import com.ania.auth.model.communication.request.CreateAccountRequest;
import com.ania.auth.model.communication.request.CredentialsAuthRequest;
import com.ania.auth.model.communication.request.OtpRequest;
import com.ania.auth.model.communication.request.SendEmailRequest;
import com.ania.auth.model.communication.response.CreateAccountResponse;
import com.ania.auth.service.MailService;
import com.ania.auth.service.UserService;
import com.ania.auth.util.AuthUtils;
import com.ania.auth.util.JwtUtils;
import com.ania.auth.util.TotpUtils;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static com.ania.auth.util.QrCodeUtil.generateQR;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LoginController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserService userService;

    @Autowired
    AuthUtils authUtils;

    @Autowired
    JwtUtils jwtUtils;


    @GetMapping("/login")
    public ModelAndView loginViewPage (HttpServletResponse response,@RequestParam(required = false) String message) throws IOException {

        authUtils.redirectIfUserAlreadyLoggedIn(response);

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

        JwtToken jwtToken = jwtUtils.generateTempJwtToken(username, Roles.PRE_AUTHENTICATED);

        ResponseCookie tokenCookie = jwtUtils.generateJwtCookie(jwtToken);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString()).body(jwtToken.getJwtToken());
    }

    @GetMapping("/second-factor")
    public ModelAndView secondFactorAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if(!authUtils.redirectIfUserAlreadyLoggedIn(response) && !authUtils.redirectIfNotAuthorized(request, response, Roles.PRE_AUTHENTICATED)) {

            String username = jwtUtils.getUserNameFromJwtToken(request);
            Boolean is2faEnabled = userService.findUserByUsername(username).getTwoFactorEnabled();

            if (!is2faEnabled) {
                JwtToken jwtToken = jwtUtils.generateJwtToken(username);

                ResponseCookie tokenCookie = jwtUtils.generateJwtCookie(jwtToken);

                response.setHeader(HttpHeaders.SET_COOKIE, tokenCookie.toString());
                response.sendRedirect("/api/content/index");
            }
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("second_factor");

        return modelAndView;
    }


    @PostMapping("/otp")
    private ResponseEntity<?> authenticateWithOTP(HttpServletRequest request, HttpServletResponse response, @Valid @RequestBody OtpRequest otpRequest) throws IOException {

        if(!authUtils.redirectIfUserAlreadyLoggedIn(response) && !authUtils.redirectIfNotAuthorized(request, response, Roles.PRE_AUTHENTICATED)) {

            String username = jwtUtils.getUserNameFromJwtToken(request);

            String secret = userService.findUserByUsername(username).getTotpSecret();

            TotpUtils.validateTotp(secret, otpRequest.getOtp());

            JwtToken jwtToken = jwtUtils.generateJwtToken(username);

            ResponseCookie tokenCookie = jwtUtils.generateJwtCookie(jwtToken);

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString()).body(jwtToken.getJwtToken());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/register-user")
    public ModelAndView registerUserViewPage (HttpServletResponse response) throws IOException {

        authUtils.redirectIfUserAlreadyLoggedIn(response); // TODO: maybe logout?

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register_user");

        return modelAndView;
    }


    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Cookie cookie = jwtUtils.clearJwtCookie();

        response.addCookie(cookie);

        response.sendRedirect("/api/auth/login");
    }

    @GetMapping("/auth-biometric")
    public ResponseEntity validateBiometricAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {

        JwtToken token = jwtUtils.getJwtFromCookies(request).get();

        User user = userService.findUserByUsername(token.getUsername());

        String date = user.getLastBiometricAuthSuccess();
        LocalDateTime localDateTime = LocalDateTime.parse(date);
        Date lastAuthDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

        long twoMinutesInMillis = 2 * 60 * 1000;
        Date twoMinutesEarlier = new Date(token.getExpirationTime().getTime() - twoMinutesInMillis);

        // Check if the current date is between the expiration date and 2 minutes earlier
        if (lastAuthDate.after(twoMinutesEarlier) && lastAuthDate.before(token.getExpirationTime())) {
            JwtToken jwtToken = jwtUtils.generateJwtToken(user.getUsername());

            ResponseCookie tokenCookie = jwtUtils.generateJwtCookie(jwtToken);

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString()).body(jwtToken.getJwtToken());
        }
        return ResponseEntity.notFound().build();
    }

}