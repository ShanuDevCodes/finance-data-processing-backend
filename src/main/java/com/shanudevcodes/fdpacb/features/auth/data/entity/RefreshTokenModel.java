package com.shanudevcodes.fdpacb.features.auth.data.entity;

import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private Instant expiresAt;
    @Column(nullable = false, updatable = false)
    private Instant issuedAt;
    @Column(nullable = false, unique = true)
    private String hashedToken;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;
    @PrePersist
    public void prePersist() {
        if (this.issuedAt == null) {
            this.issuedAt = Instant.now();
        }
    }
}
