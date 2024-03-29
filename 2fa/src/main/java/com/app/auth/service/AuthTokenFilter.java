package com.app.auth.service;

import com.app.auth.model.JwtToken;
import com.app.auth.service.util.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
public class AuthTokenFilter extends GenericFilterBean {

    @Autowired
    private UserService userService;

    @Autowired
    JwtUtils jwtUtils;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            Optional<JwtToken> optionalJwt = jwtUtils.getJwtFromCookies(request);

            if (optionalJwt.isPresent()) {

                JwtToken jwtToken = optionalJwt.get();

                Boolean isTemp = jwtUtils.isTemp(jwtToken.getJwtToken());

                if (jwtUtils.validateJwtToken(jwtToken.getJwtToken()) && !isTemp) {
                    setAuthentication(httpRequest, jwtToken);
                } else {
                    SecurityContextHolder.clearContext();
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            httpResponse.addCookie(jwtUtils.clearJwtCookie());
        } finally {
            filterChain.doFilter(request, response);
        }
    }

    private void setAuthentication(HttpServletRequest request, JwtToken jwtToken) {
        String username = jwtToken.getUsername();

        UserDetails userDetails = userService.loadUserByUsername(username);

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}