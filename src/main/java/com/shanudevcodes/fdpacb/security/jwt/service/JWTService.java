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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JWTService {
    @Value("${JWT_SECRET_BASE64}") private String jwtSecret;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    final Long accessTokenValidityMs = 15L * 60L * 1000L;
    public final Long refreshTokenValidityMs = 30L * 24L * 60L * 60L * 1000L;

    public Claims parseAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String generateToken(
            UserModel user,
            JwtType type,
            Long expiry
    ){
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiry);
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(user.getId().toString())
                .claim("type", type.name())
                .claim("roles", user.getRoles()
                        .stream()
                        .map(Enum::name)
                        .toList())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setIssuer("fdpacb-backend")
                .setAudience("fdpacb-client")
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

    public Set<Role> getUserRoles(String token){
        Claims claims = parseAllClaims(token);
        List<String> roles = claims.get("roles", List.class);
        return roles.stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }
}
