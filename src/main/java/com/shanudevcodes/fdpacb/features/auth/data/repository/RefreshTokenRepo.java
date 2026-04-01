package com.shanudevcodes.fdpacb.features.auth.data.repository;

import com.shanudevcodes.fdpacb.features.auth.data.entity.RefreshTokenModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepo extends JpaRepository<RefreshTokenModel, UUID> {
    Optional<RefreshTokenModel> findByUser_IdAndHashedToken(UUID userId, String hashedToken);
    void deleteByUser_IdAndHashedToken(UUID userId, String hashedToken);
}
