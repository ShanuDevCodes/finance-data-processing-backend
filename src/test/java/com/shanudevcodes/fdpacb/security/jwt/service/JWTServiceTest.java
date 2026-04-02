package com.shanudevcodes.fdpacb.security.jwt.service;

import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import com.shanudevcodes.fdpacb.security.rbac.role.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JWTServiceTest {

    private JWTService jwtService;
    private UserModel testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JWTService();
        String fakeSecret = Base64.getEncoder().encodeToString("super-secret-key-that-is-at-least-32-bytes-long!".getBytes());
        ReflectionTestUtils.setField(jwtService, "jwtSecret", fakeSecret);
        jwtService.init();

        testUser = UserModel.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .roles(Set.of(Role.ADMIN, Role.VIEWER))
                .build();
    }

    @Test
    void testGenerateAccessToken_ContainsCorrectClaims() {
        String token = jwtService.generateAccessToken(testUser);
        
        assertNotNull(token);
        Claims claims = jwtService.parseAllClaims(token);
        
        assertEquals(testUser.getId().toString(), claims.getSubject());
        assertEquals("test@example.com", claims.get("email", String.class));
        assertEquals("ACCESS_TOKEN", claims.get("type", String.class));
        
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ADMIN"));
        assertTrue(roles.contains("VIEWER"));
    }

    @Test
    void testGenerateRefreshToken_HasDifferentType() {
        String token = jwtService.generateRefreshToken(testUser);
        
        assertNotNull(token);
        Claims claims = jwtService.parseAllClaims(token);
        assertEquals("REFRESH_TOKEN", claims.get("type", String.class));
    }
}
