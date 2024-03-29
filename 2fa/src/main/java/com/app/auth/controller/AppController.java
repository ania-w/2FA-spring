package com.app.auth.controller;

import com.app.auth.model.JwtToken;
import com.app.auth.service.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/content")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AppController {

    @Autowired
    JwtUtils jwtUtils;

    @GetMapping("/index")
    public String index(Model model, HttpServletRequest request) {

        JwtToken jwtToken = jwtUtils.getJwtFromCookies(request).orElseThrow();

        model.addAttribute("username", jwtToken.getUsername());
        model.addAttribute("jwtToken", jwtToken.getJwtToken());
        model.addAttribute("expirationTime", jwtToken.getExpirationTime());

        return "welcome";
    }

}
