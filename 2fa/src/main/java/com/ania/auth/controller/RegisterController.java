package com.ania.auth.controller;

import com.ania.auth.model.JwtToken;
import com.ania.auth.model.Roles;
import com.ania.auth.model.User;
import com.ania.auth.model.communication.request.CreateAccountRequest;
import com.ania.auth.model.communication.request.SendEmailRequest;
import com.ania.auth.model.communication.response.CreateAccountResponse;
import com.ania.auth.model.communication.response.User2faData;
import com.ania.auth.service.MailService;
import com.ania.auth.service.UserService;
import com.ania.auth.util.AuthUtils;
import com.ania.auth.util.JwtUtils;
import com.ania.auth.util.QrCodeUtil;
import com.ania.auth.util.TotpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

import static com.ania.auth.util.QrCodeUtil.generateQR;

@Controller
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RegisterController {

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    MailService mailService;

    @Autowired
    AuthUtils authUtils;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/register-user")
    public ResponseEntity<?> registerUser(@Valid @RequestBody CreateAccountRequest request) throws Exception {

        User2faData user2faData = new User2faData(request.getUsername());
        byte[] qrCode = QrCodeUtil.generateQR(user2faData.toString());
        String rsaPrivate = Base64.getEncoder().encodeToString(user2faData.getKeyPair().getPrivate().getEncoded());

        JwtToken jwtToken = jwtUtils.generateTempJwtToken(request.getUsername(), Roles.PRE_REGISTERED);
        ResponseCookie tokenCookie = jwtUtils.generateJwtCookie(jwtToken);
        ResponseEntity<?> response = ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, tokenCookie.toString()).body(new CreateAccountResponse(qrCode));

        User user = new User(request.getEmail(), request.getUsername(), encoder.encode(request.getPassword()),
                user2faData.getTotpSecret(), user2faData.getDeviceId(), rsaPrivate , false, null);

        userService.save(user);

        return response;
    }

    @PostMapping("/send-email")
    public ResponseEntity<?> sendEmail(@Valid @RequestBody SendEmailRequest emailRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        String content = QrCodeUtil.extractQRCodeContent(emailRequest.getQrCodeData());

        if(!authUtils.redirectIfNotAuthorized(request, response, Roles.PRE_REGISTERED)) {
            mailService.sendAuthDataViaEmail(emailRequest.getEmail(), content);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/enable-2fa")
    public void enable2Fa(@Valid @RequestBody CreateAccountRequest accountRequest, HttpServletResponse response, HttpServletRequest request) throws Exception {
        authUtils.redirectIfNotAuthorized(request, response, Roles.PRE_REGISTERED);
        User user = userService.findUserByUsername(accountRequest.getUsername());
        user.setTwoFactorEnabled(true);
        userService.update(user);
    }

}
