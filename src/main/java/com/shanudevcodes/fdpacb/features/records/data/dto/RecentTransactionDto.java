package com.shanudevcodes.fdpacb.features.records.data.dto;

import com.shanudevcodes.fdpacb.features.records.data.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecentTransactionDto {
    private BigDecimal amount;
    private Category category;
    private LocalDate date;
}
