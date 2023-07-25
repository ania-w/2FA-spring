package com.ania.auth.util;

import com.ania.auth.model.JwtToken;
import com.ania.auth.model.Roles;
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
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;


@Component
public class JwtUtils {

    @Value("${ania.app.jwtSecret}")
    private String jwtSecret;

    @Value("${ania.app.jwtCookieName}")
    private String jwtCookieName;

    private final static Long EXPIRATION_TIME = 60 * 60 * 1000L;
    private final static Long TEMP_EXPIRATION_TIME = 2 * 60 * 1000L;


    public JwtToken generateJwtToken(String username) {

        Date expirationDate = new Date((new Date()).getTime() + EXPIRATION_TIME);

        String jwt = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();

        return new JwtToken(jwt, username, expirationDate);
    }

    public Boolean hasRole(String jwtToken, Roles role){
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(jwtToken)
                .getBody();

        return claims.get(role.name(), Boolean.class) != null;
    }

    public JwtToken generateTempJwtToken(String username, Roles role) {

        Date expirationDate = new Date((new Date()).getTime() + TEMP_EXPIRATION_TIME);

        Claims claims = Jwts.claims().setSubject(username);
        claims.put(role.name(), true);

        String jwt = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .addClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();

        return new JwtToken(jwt, username,expirationDate);
    }

    public Optional<JwtToken> getJwtFromCookies(ServletRequest request) {
        Cookie cookie = WebUtils.getCookie((HttpServletRequest) request, jwtCookieName);

        if(cookie != null) {
            String jwtToken = cookie.getValue();
            String username = getUserNameFromJwtToken(jwtToken);
            Date expirationDate = getExpirationDateFromJwtToken(jwtToken);

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

    public Date getExpirationDateFromJwtToken(String jwtToken) {
        Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(jwtToken).getBody();
        return claims.getExpiration();
    }

    public String getUserNameFromJwtToken(String jwtToken) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(jwtToken).getBody().getSubject();
    }

    public String getUserNameFromJwtToken(HttpServletRequest request) {
        String jwtToken = Arrays.stream(request.getCookies()).filter(c -> c.getName().equals(jwtCookieName)).findFirst().get().getValue();
        return getUserNameFromJwtToken(jwtToken);
    }

    public String getJwtTokenFromRequest(HttpServletRequest request) {
        return Arrays.stream(request.getCookies()).filter(c -> c.getName().equals(jwtCookieName)).findFirst().get().getValue();
    }

    public Boolean isJwtTokenPresent(HttpServletRequest request){
        if(request.getCookies() == null)
            return false;

        return Arrays.stream(request.getCookies()).anyMatch(c -> c.getName().equals(jwtCookieName));
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

    public Boolean isTemp(String jwtToken) {
        return hasRole(jwtToken,Roles.PRE_AUTHENTICATED) || hasRole(jwtToken,Roles.PRE_REGISTERED);
    }
}