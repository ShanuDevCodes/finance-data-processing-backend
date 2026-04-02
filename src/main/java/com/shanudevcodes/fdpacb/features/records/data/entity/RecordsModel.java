package com.shanudevcodes.fdpacb.features.records.data.entity;

import com.shanudevcodes.fdpacb.features.records.data.enums.Category;
import com.shanudevcodes.fdpacb.features.records.data.enums.PaymentMethod;
import com.shanudevcodes.fdpacb.features.records.data.enums.RecordStatus;
import com.shanudevcodes.fdpacb.features.records.data.enums.RecordType;
import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        indexes = {
                @Index(name = "idx_user_date", columnList = "user_id,transactionDate"),
                @Index(name = "idx_user_type", columnList = "user_id,type"),
                @Index(name = "idx_user_category", columnList = "user_id,category")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordsModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;
    @Column(nullable = false, updatable = false)
    private LocalDate transactionDate;
    private String note;
    @Enumerated(EnumType.STRING)
    private RecordStatus status;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    @Column(unique = true)
    private String referenceId;
    private Boolean isRecurring = false;
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
    private Boolean isDeleted;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;
    @PrePersist
    public void prePersist() {
        if (this.transactionDate == null) {
            this.transactionDate = LocalDate.now();
        }
        if (this.status == null) {
            this.status = RecordStatus.PENDING;
        }
        if (this.isDeleted == null){
            this.isDeleted = false;
        }
    }
}
