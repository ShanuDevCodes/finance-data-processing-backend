package com.shanudevcodes.fdpacb.features.auth.domain.service;

import com.shanudevcodes.fdpacb.common.util.HashPassEncoder;
import com.shanudevcodes.fdpacb.features.auth.data.dto.LoginRequest;
import com.shanudevcodes.fdpacb.features.auth.data.dto.SignUpResponse;
import com.shanudevcodes.fdpacb.features.auth.data.dto.SignupRequest;
import com.shanudevcodes.fdpacb.features.auth.data.dto.TokenPair;
import com.shanudevcodes.fdpacb.features.auth.data.entity.RefreshTokenModel;
import com.shanudevcodes.fdpacb.features.auth.data.repository.RefreshTokenRepo;
import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import com.shanudevcodes.fdpacb.features.users.data.repository.UserRepo;
import com.shanudevcodes.fdpacb.security.jwt.service.JWTService;
import com.shanudevcodes.fdpacb.security.rbac.role.Role;
import com.shanudevcodes.fdpacb.security.rbac.role.Status;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final HashPassEncoder hashPassEncoder;
    private final UserRepo userRepo;
    private final JWTService jwtService;
    private final RefreshTokenRepo refreshTokenRepo;

    private boolean validateUserCredentials(String email, String password) {
        if (email.isBlank() || password.isBlank()) return false;
        String trimmedEmail = email.trim();
        Optional<UserModel> user = userRepo.findByEmail(trimmedEmail);
        if (user.isEmpty()) return false;
        return hashPassEncoder.matches(password, user.get().getHashedPassword());
    }

    private String hashToken(String token){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    @Transactional
    void storeRefreshToken(UserModel user, String rawRefreshToken){
        String hashedRefreshToken = hashToken(rawRefreshToken);
        long expiryMs = jwtService.refreshTokenValidityMs;
        Instant expiresAt = Instant.now().plusMillis(expiryMs);
        refreshTokenRepo.save(
                RefreshTokenModel.builder()
                        .expiresAt(expiresAt)
                        .hashedToken(hashedRefreshToken)
                        .user(user)
                        .build()
        );
    }

    @Transactional
    public SignUpResponse createUser(SignupRequest request, Set<Role> roles) {
        String hashedPassword = hashPassEncoder.encodePassword(request.getPassword());
        if (hashedPassword == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid password");
        }
        Optional<UserModel> existingUser = userRepo.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }
        UserModel newUser = userRepo.save(
                UserModel.builder()
                        .name(request.getName())
                        .email(request.getEmail())
                        .hashedPassword(hashedPassword)
                        .roles(roles)
                        .status(Status.ACTIVE)
                        .build()
        );
        return new SignUpResponse(
                newUser.getName(),
                newUser.getEmail()
        );
    }

    @Transactional
    public TokenPair loginUser(LoginRequest request) {
        String trimmedEmail = request.email.trim();
        Optional<UserModel> user = userRepo.findByEmail(trimmedEmail);
        if (user.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        boolean isPasswordValid = validateUserCredentials(request.email, request.password);
        if (!isPasswordValid) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        String newAccessToken = jwtService.generateAccessToken(user.get());
        String newRefreshToken = jwtService.generateRefreshToken(user.get());
        storeRefreshToken(user.get(),newRefreshToken);
        return new TokenPair(
                newAccessToken,
                newRefreshToken
        );
    }

    @Transactional
    public TokenPair refresh (String refreshToken){
        try {
            if (!jwtService.validateRefreshToken(refreshToken)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh Token expired or invalid");
        }
        UUID userId = jwtService.getUserIdFromToken(refreshToken);
        if (userId == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Fount");
        }
        Optional<UserModel> user = userRepo.findById(userId);
        if (user.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User Not Found");
        }
        String hashed = hashToken(refreshToken);
        if (refreshTokenRepo.findByUser_IdAndHashedToken(user.get().getId(),hashed).isEmpty()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Refresh Token Not recognised");
        }
        refreshTokenRepo.deleteByUser_IdAndHashedToken(user.get().getId(),hashed);
        String newAccessToken = jwtService.generateAccessToken(user.get());
        String newRefreshToken = jwtService.generateRefreshToken(user.get());
        storeRefreshToken(user.get(),newRefreshToken);
        return new TokenPair(
                newAccessToken,
                newRefreshToken
        );
    }
}
