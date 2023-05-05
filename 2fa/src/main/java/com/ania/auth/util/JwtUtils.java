package com.ania.auth.util;

import com.ania.auth.model.JwtToken;
import com.ania.auth.service.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;


@Component
public class JwtUtils {

    @Value("${ania.app.jwtSecret}")
    private String jwtSecret;

    @Value("${ania.app.jwtCookieName}")
    private String jwtCookieName;

    private final static Long EXPIRATION_TIME = 60 * 60 * 1000L;

    public JwtToken generateJwtToken(UserDetailsImpl userDetails) {

        Date expirationDate = new Date((new Date()).getTime() + EXPIRATION_TIME);

        String jwt = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();

        return new JwtToken(jwt, userDetails.getUsername(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(expirationDate));
    }

    public Optional<JwtToken> getJwtFromCookies(ServletRequest request) {
        Cookie cookie = WebUtils.getCookie((HttpServletRequest) request, jwtCookieName);

        if(cookie != null) {
            String jwtToken = cookie.getValue();
            String username = getUserNameFromJwtToken(jwtToken);
            String expirationDate = getExpirationDateFromJwtToken(jwtToken);

            return Optional.of(new JwtToken(jwtToken, username, expirationDate));
        }

        return Optional.empty();

    }

    public ResponseCookie generateJwtCookie(JwtToken jwtToken) {

        return ResponseCookie.from(jwtCookieName, jwtToken.getJwtToken())
                .httpOnly(true)
                .secure(true)
                .path("/api")
                .maxAge(EXPIRATION_TIME)
                .sameSite("None")
                .build();
    }

    public Cookie clearJwtCookie() {

        Cookie cookie = new Cookie(jwtCookieName, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/api");
        cookie.setSecure(true);
        cookie.setMaxAge(0);

        return cookie;
    }

    public String getExpirationDateFromJwtToken(String jwtToken) {
        Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(jwtToken).getBody();
        Date expirationDate = claims.getExpiration();
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(expirationDate);
    }

    public String getUserNameFromJwtToken(String jwtToken) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(jwtToken).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
        } catch (Exception e) {
            System.err.println("Error while validating JWT: " + e.getMessage());
            return false;
        }
        return true;
    }
}