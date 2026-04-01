package com.shanudevcodes.fdpacb.security.jwt.service;

import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import com.shanudevcodes.fdpacb.security.jwt.util.JwtType;
import com.shanudevcodes.fdpacb.security.rbac.role.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class JWTService {
    @Value("${JWT_SECRET_BASE64}") private String jwtSecret;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    private final Long accessTokenValidityMs = 15L * 60L * 1000L;
    private final Long refreshTokenValidityMs = 30L * 24L * 60L * 60L * 1000L;

    public Claims parseAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new RuntimeException("Invalid or expired JWT token", e);
        }
    }

    private String generateToken(
            UserModel user,
            JwtType type,
            Long expiry
    ){
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiry);
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("type", type.name())
                .claim("role", user.getRole().name())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

    }

    public String generateAccessToken(
            UserModel user
    ){
        return generateToken(
                user,
                JwtType.ACCESS_TOKEN,
                accessTokenValidityMs
        );
    }

    public String generateRefreshToken(
            UserModel user
    ){
        return generateToken(
                user,
                JwtType.REFRESH_TOKEN,
                refreshTokenValidityMs
        );
    }

    public boolean validateAccessToken(String token) {
        try {
            Claims claims = parseAllClaims(token);
            String tokenType = claims.get("type", String.class);
            return JwtType.ACCESS_TOKEN.name().equals(tokenType) && claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token){
        try {
            Claims claims = parseAllClaims(token);
            String tokenType = claims.get("type", String.class);
            return JwtType.REFRESH_TOKEN.name().equals(tokenType) && claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public UUID getUserIdFromToken(String token){
        Claims claims = parseAllClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    public Role getUserRole(String token){
        Claims claims = parseAllClaims(token);
        return Role.valueOf(claims.get("role", String.class));
    }
}
