package com.ania.auth.controller;

import com.ania.auth.model.JwtToken;
import com.ania.auth.util.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping("/api/content")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AppController {

    @Autowired
    JwtUtils jwtUtils;


    @GetMapping("/index")
    public String index(Model model, HttpServletRequest request) {

        JwtToken jwtToken = jwtUtils.getJwtFromCookies(request);

        model.addAttribute("username", jwtToken.getUsername());
        model.addAttribute("jwtToken", jwtToken.getJwtToken());
        model.addAttribute("expirationTime", jwtToken.getExpirationTime());

        return "welcome";
    }

}
