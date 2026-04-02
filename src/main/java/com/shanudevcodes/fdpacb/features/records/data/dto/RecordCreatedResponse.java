package com.shanudevcodes.fdpacb.features.records.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordCreatedResponse {
    private UUID id;
    private BigDecimal amount;
    private String type;
    private String category;
    private LocalDate transactionDate;
    private String status;
}
