package com.ania.auth.controller;

import com.ania.auth.model.JwtToken;
import com.ania.auth.model.Roles;
import com.ania.auth.model.TwoFactorMethod;
import com.ania.auth.model.User;
import com.ania.auth.model.communication.request.CreateAccountRequest;
import com.ania.auth.model.communication.request.CredentialsAuthRequest;
import com.ania.auth.model.communication.request.OtpRequest;
import com.ania.auth.model.communication.request.SendEmailRequest;
import com.ania.auth.model.communication.response.CreateAccountResponse;
import com.ania.auth.service.MailService;
import com.ania.auth.service.UserService;
import com.ania.auth.util.JwtUtils;
import com.ania.auth.util.QrCodeUtil;
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

import static com.ania.auth.util.QrCodeUtil.generateQR;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    MailService mailService;


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
        return TotpUtils.getTotpPassword(secretKey);
    }

    @GetMapping("/get-users")
    public void users(String secret) {
        userService.printAllUsers();
    }

    private Boolean redirectIfUserAlreadyLoggedIn(HttpServletResponse response) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()) {
            response.sendRedirect("/api/content/index");
            return true;
        }
        return false;
    }

    private Boolean redirectIfNotAuthorized(HttpServletRequest request, HttpServletResponse response, Roles role) throws IOException {

        if(jwtUtils.isJwtTokenPresent(request)) {

            String jwtToken = jwtUtils.getJwtTokenFromRequest(request);
            if (!jwtUtils.hasRole(jwtToken,role)) {
                response.sendRedirect("/api/auth/login");
                return true;
            }

        } else {
            response.sendRedirect("/api/auth/login");
            return true;
        }
        return false;
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

    @GetMapping("/otp")
    public ModelAndView otpViewPage(HttpServletRequest request, HttpServletResponse response) throws IOException {

        redirectIfUserAlreadyLoggedIn(response);
        redirectIfNotAuthorized(request, response, Roles.PRE_AUTHENTICATED);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("otp");

        return modelAndView;
    }

    @GetMapping("/choose-second-factor")
    public void secondFactor(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if(!redirectIfUserAlreadyLoggedIn(response) && !redirectIfNotAuthorized(request, response, Roles.PRE_AUTHENTICATED)) {

            String username = jwtUtils.getUserNameFromJwtToken(request);
            Boolean is2faEnabled = userService.findUserByUsername(username).getTwoFactorEnabled();

            String twoFactorMethod = userService.findUserByUsername(username).getTwoFactorMethod();

            switch (twoFactorMethod) {
                case TwoFactorMethod.OTP -> response.sendRedirect("/api/auth/otp");
                case TwoFactorMethod.BIOMETRICS -> response.sendRedirect("/api/auth/biometrics");
            }

            if (twoFactorMethod.equals(TwoFactorMethod.NONE) && !is2faEnabled) {
                JwtToken jwtToken = jwtUtils.generateJwtToken(username);

                ResponseCookie tokenCookie = jwtUtils.generateJwtCookie(jwtToken);

                response.setHeader(HttpHeaders.SET_COOKIE, tokenCookie.toString());
                response.sendRedirect("/api/content/index");
            }
        }

    }


    @PostMapping("/otp")
    private ResponseEntity<?> authenticateWithOTP(HttpServletRequest request, HttpServletResponse response, @Valid @RequestBody OtpRequest otpRequest) throws IOException {

        if(!redirectIfUserAlreadyLoggedIn(response) && !redirectIfNotAuthorized(request, response, Roles.PRE_AUTHENTICATED)) {

            String username = jwtUtils.getUserNameFromJwtToken(request);

            String secret = userService.findUserByUsername(username).getSecret();

            TotpUtils.validateTotp(secret, otpRequest.getOtp());

            JwtToken jwtToken = jwtUtils.generateJwtToken(username);

            ResponseCookie tokenCookie = jwtUtils.generateJwtCookie(jwtToken);

            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString()).body(jwtToken.getJwtToken());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/register-user")
    public ModelAndView registerUserViewPage (HttpServletResponse response) throws IOException {

        redirectIfUserAlreadyLoggedIn(response); // TODO: maybe logout?

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register_user");

        return modelAndView;
    }

    @PostMapping("/send-email")
    public ResponseEntity<?> sendEmail(@Valid @RequestBody SendEmailRequest sendEmailRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(!redirectIfNotAuthorized(request, response, Roles.PRE_REGISTERED)) {
            mailService.sendSecretViaEmail(sendEmailRequest.getEmail(), userService.findUserByUsername(sendEmailRequest.getUsername()).getSecret());
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register-user")
    public ResponseEntity<?> registerUser(@Valid @RequestBody CreateAccountRequest request) throws Exception {

        String twoFactorMethod = request.getTwoFactorMethod();

        String secret = null;
        ResponseEntity<?> response = null;

        if(twoFactorMethod.equals(TwoFactorMethod.OTP)) {
            secret = TotpUtils.getKey();
            JwtToken jwtToken = jwtUtils.generateTempJwtToken(request.getUsername(), Roles.PRE_REGISTERED);
            ResponseCookie tokenCookie = jwtUtils.generateJwtCookie(jwtToken);
            response = ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString()).body(new CreateAccountResponse(generateQR(secret)));
        } else if(twoFactorMethod.equals(TwoFactorMethod.BIOMETRICS)) {
            //TODO
        } else {
            response = ResponseEntity.ok().build();
        }

        User user = new User(request.getUsername(), encoder.encode(request.getPassword()), request.getEmail(), secret, twoFactorMethod, false);

        userService.save(user);

        return response;
    }

    @PostMapping("/enable-2fa")
    public void enable2Fa(@Valid @RequestBody CreateAccountRequest accountRequest,HttpServletResponse response, HttpServletRequest request) throws Exception {
        redirectIfNotAuthorized(request, response, Roles.PRE_REGISTERED);

    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Cookie cookie = jwtUtils.clearJwtCookie();

        response.addCookie(cookie);

        response.sendRedirect("/api/auth/login");
    }

}