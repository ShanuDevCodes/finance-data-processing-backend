package com.shanudevcodes.fdpacb.features.records.data.dto;

import com.shanudevcodes.fdpacb.features.records.data.enums.Category;
import com.shanudevcodes.fdpacb.features.records.data.enums.RecordStatus;
import com.shanudevcodes.fdpacb.features.records.data.enums.RecordType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse{
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;
    private Map<Category, BigDecimal> categoryBreakdown;
    private Map<RecordType, BigDecimal> typeBreakdown;
    private Map<RecordStatus, BigDecimal> statusBreakdown;
    private List<RecentTransactionDto> recentTransactions;
}