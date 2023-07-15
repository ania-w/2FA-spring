package com.ania.auth.util;

import com.ania.auth.model.Roles;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthUtils {

    @Autowired
    JwtUtils jwtUtils;

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
