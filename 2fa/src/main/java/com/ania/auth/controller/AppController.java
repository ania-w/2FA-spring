package com.ania.auth.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AppController {

    @GetMapping("/")
    public ModelAndView messages() {
        ModelAndView mav = new ModelAndView("welcome");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        mav.addObject("username", username);
        return mav;
    }

}
