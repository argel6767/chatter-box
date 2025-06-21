package com.chat_room_app.jwt;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;


@Service
public class JwtService {

    @Value("${secret.key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private Long expirationTime;

    @Value("${is.secure.cookie}")
    private boolean isSecureCookie;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /*
     * generates JWT Token
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, expirationTime);
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    /*
     * Builds the JWT token using JWT builder
     */
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /*
     * check if token is a valid token and is attached to user
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Generates cookie header using the generated jwt token
     * @param token
     * @return
     */
    public String generateCookie(String token, Optional<Long> expirationTime) {
        long expiration = expirationTime.orElse(getExpirationTime());
        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(isSecureCookie); // For HTTPS, will be false during DEV, true during PROD
        jwtCookie.setPath("/"); // Global path
        jwtCookie.setMaxAge((int) (expiration/ 1000)); // Convert from ms to seconds
        String cookieHeader = String.format("%s=%s; Max-Age=%d; Path=%s; HttpOnly; Secure; SameSite=Strict",
                jwtCookie.getName(), jwtCookie.getValue(), jwtCookie.getMaxAge(), jwtCookie.getPath());
        return cookieHeader;
    }

    /*
     * checks if token is expired, if so then it is no longer valid
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
