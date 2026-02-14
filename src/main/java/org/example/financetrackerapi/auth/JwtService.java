package org.example.financetrackerapi.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.example.financetrackerapi.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${security.jwt.secret}")
    private String secrets;
    @Value("${security.jwt.expiration}")
    private long expiration;

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role",user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }


    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaims(token, claims-> claims.get("role", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }


    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    private Key getSigningKey(){
        byte[] secretBytes = Base64.getDecoder().decode(secrets);
        return Keys.hmacShaKeyFor(secretBytes);
    }
}
