package com.app.auth.controller;

import com.app.auth.model.communication.response.User2faData;
import com.app.auth.model.JwtToken;
import com.app.auth.model.Roles;
import com.app.auth.model.User;
import com.app.auth.model.communication.request.CreateAccountRequest;
import com.app.auth.model.communication.request.SendEmailRequest;
import com.app.auth.model.communication.response.CreateAccountResponse;
import com.app.auth.service.util.MailUtil;
import com.app.auth.service.UserService;
import com.app.auth.service.util.JwtUtils;
import com.app.auth.service.util.QrCodeUtil;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Controller
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RegisterController {

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    MailUtil mailUtil;

    @Autowired
    JwtUtils jwtUtils;

    @GetMapping("/test-users")
    public void test_users() {

        String username = "test";
        String password="password";

        User2faData user2faData = new User2faData(username);
        String rsaPrivate = Base64.getEncoder().encodeToString(user2faData.getKeyPair().getPrivate().getEncoded());
        String rsaPublic = Base64.getEncoder().encodeToString(user2faData.getKeyPair().getPublic().getEncoded());
        System.out.println(rsaPublic);

        for(int i = 0; i< 10; i++){
            User user = new User("test"+i+"@mail.com", username+i, encoder.encode(password),
                    user2faData.getTotpSecret(), user2faData.getDeviceId(), rsaPrivate, true, null);
            userService.save(user);

        }

        userService.printAllUsers();
    }

    @PostMapping("/register-user")
    public ResponseEntity<?> registerUser(@Valid @RequestBody CreateAccountRequest request) {

        ResponseEntity<?> response;

        try {

            User2faData user2faData = new User2faData(request.getUsername());
            byte[] qrCode = QrCodeUtil.generateQR(user2faData.toString());
            String rsaPrivate = Base64.getEncoder().encodeToString(user2faData.getKeyPair().getPrivate().getEncoded());

            JwtToken jwtToken = jwtUtils.generateTempJwtToken(request.getUsername(), Roles.PRE_REGISTERED);
            ResponseCookie tokenCookie = jwtUtils.generateJwtCookie(jwtToken);
            response = ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString()).body(new CreateAccountResponse(qrCode));

            User user = new User(request.getEmail(), request.getUsername(), encoder.encode(request.getPassword()),
                    user2faData.getTotpSecret(), user2faData.getDeviceId(), rsaPrivate, false, null);

            userService.save(user);
        } catch (Exception e) {
            response = ResponseEntity.badRequest().body(e.getMessage());
        }

        return response;
    }

    @PostMapping("/send-email")
    public ResponseEntity<?> sendEmail(@Valid @RequestBody SendEmailRequest emailRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        if(!redirectIfNotAuthorized(request, response, Roles.PRE_REGISTERED)) {
            String content = QrCodeUtil.extractQRCodeContent(emailRequest.getQrCodeData());
            mailUtil.sendAuthDataViaEmail(emailRequest.getEmail(), content);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/activate")
    public ResponseEntity<?> enable2Fa(@Nullable @RequestBody CreateAccountRequest accountRequest, HttpServletResponse response, HttpServletRequest request) throws Exception {
        if(redirectIfNotAuthorized(request, response, Roles.PRE_REGISTERED))
            throw new RuntimeException();
        String username = jwtUtils.getUserNameFromJwtToken(jwtUtils.getJwtTokenFromRequest(request));
        User user = userService.findUserByUsername(username);
        user.setIsActive(true);
        userService.update(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/activate")
    public ModelAndView activate(HttpServletResponse response, HttpServletRequest request) throws Exception {
        if(redirectIfNotAuthorized(request, response, Roles.PRE_REGISTERED))
            throw new RuntimeException();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("activate");
        return modelAndView;
    }

    @PostMapping("/get-activation-data")
    public ResponseEntity<?> getData(HttpServletRequest request) throws Exception {
        String username = jwtUtils.getUserNameFromJwtToken(jwtUtils.getJwtTokenFromRequest(request));
        User2faData user2faData = new User2faData(username);
        byte[] qrCode = QrCodeUtil.generateQR(user2faData.toString());
        String rsaPrivate = Base64.getEncoder().encodeToString(user2faData.getKeyPair().getPrivate().getEncoded());
        User user = userService.findUserByUsername(username);
        user.setDeviceId(user2faData.getDeviceId());
        user.setTotpSecret(user2faData.getTotpSecret());
        user.setPrivateKey(rsaPrivate);
        JwtToken jwtToken = jwtUtils.generateTempJwtToken(username, Roles.PRE_REGISTERED);
        ResponseCookie tokenCookie = jwtUtils.generateJwtCookie(jwtToken);
        userService.update(user);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString()).body(new CreateAccountResponse(qrCode));
    }




    @GetMapping("/register-user")
    public ModelAndView registerUserViewPage (HttpServletResponse response) throws IOException {

        redirectIfUserAlreadyLoggedIn(response);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register_user");

        return modelAndView;
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

    public Boolean redirectIfUserAlreadyLoggedIn(HttpServletResponse response) throws IOException {
        if(isLoggedIn()) {
            response.sendRedirect("/api/content/index");
            return true;
        }
        return false;
    }

}
