package com.shanudevcodes.fdpacb.features.records.domain.service;

import com.shanudevcodes.fdpacb.features.records.data.dto.DashboardResponse;
import com.shanudevcodes.fdpacb.features.records.data.dto.RecentTransactionDto;
import com.shanudevcodes.fdpacb.features.records.data.entity.RecordsModel;
import com.shanudevcodes.fdpacb.features.records.data.enums.Category;
import com.shanudevcodes.fdpacb.features.records.data.enums.RecordStatus;
import com.shanudevcodes.fdpacb.features.records.data.enums.RecordType;
import com.shanudevcodes.fdpacb.features.records.data.repository.RecordsRepo;
import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import com.shanudevcodes.fdpacb.features.users.data.repository.UserRepo;
import com.shanudevcodes.fdpacb.security.rbac.role.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecordAnalyticsService {
    private final RecordsRepo recordsRepo;
    private final UserRepo userRepo;

    public DashboardResponse getDashboard(UserModel user, List<UUID> targetUserIds) {
        List<UUID> userIds = getAccessibleUserIds(user, targetUserIds);
        BigDecimal income = recordsRepo.sumByType(userIds, RecordType.INCOME);
        BigDecimal expense = recordsRepo.sumByType(userIds, RecordType.EXPENSE);
        Map<Category, BigDecimal> categoryMap = mapToCategory(recordsRepo.categoryBreakdown(userIds));
        Map<RecordType, BigDecimal> typeMap = mapToType(recordsRepo.typeBreakdown(userIds));
        Map<RecordStatus, BigDecimal> statusMap = mapToStatus(recordsRepo.statusBreakdown(userIds));
        List<RecentTransactionDto> recent = recordsRepo.findRecent(userIds)
                .stream()
                .map(this::toDto)
                .toList();
        return DashboardResponse.builder()
                .totalIncome(income)
                .totalExpense(expense)
                .netBalance(income.subtract(expense))
                .categoryBreakdown(categoryMap)
                .typeBreakdown(typeMap)
                .statusBreakdown(statusMap)
                .recentTransactions(recent)
                .build();
    }

    private List<UUID> getAccessibleUserIds(UserModel user, List<UUID> targetUserIds) {
        if (targetUserIds != null && !targetUserIds.isEmpty()) {
            if (user.getRoles().contains(Role.ADMIN)) {
                return targetUserIds;
            }
            if (user.getRoles().contains(Role.ANALYST)) {
                List<UUID> assignedIds = userRepo.findUserModelByAssignedAnalyst_Id(user.getId())
                        .stream().map(UserModel::getId).toList();
                for (UUID targetId : targetUserIds) {
                    if (!targetId.equals(user.getId()) && !assignedIds.contains(targetId)) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to view data for one or more users in the target list");
                    }
                }
                return targetUserIds;
            }
            for (UUID targetId : targetUserIds) {
                if (!targetId.equals(user.getId())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to view data for one or more users in the target list");
                }
            }
            return targetUserIds;
        }
        if (user.getRoles().contains(Role.ADMIN)) {
            return userRepo.findAll().stream().map(UserModel::getId).toList();
        }
        if (user.getRoles().contains(Role.ANALYST)) {
            List<UUID> ids = new ArrayList<>(
                    userRepo.findUserModelByAssignedAnalyst_Id(user.getId())
                            .stream()
                            .map(UserModel::getId)
                            .toList()
            );
            ids.add(user.getId());
            return ids;
        }
        return List.of(user.getId());
    }

    private Map<Category, BigDecimal> mapToCategory(List<Object[]> data) {
        return data.stream()
                .collect(Collectors.toMap(
                        row -> (Category) row[0],
                        row -> (BigDecimal) row[1]
                ));
    }

    private Map<RecordType, BigDecimal> mapToType(List<Object[]> data) {
        return data.stream()
                .collect(Collectors.toMap(
                        row -> (RecordType) row[0],
                        row -> (BigDecimal) row[1]
                ));
    }

    private Map<RecordStatus, BigDecimal> mapToStatus(List<Object[]> data) {
        return data.stream()
                .collect(Collectors.toMap(
                        row -> (RecordStatus) row[0],
                        row -> (BigDecimal) row[1]
                ));
    }

    private RecentTransactionDto toDto(RecordsModel r) {
        return RecentTransactionDto.builder()
                .amount(r.getAmount())
                .category(r.getCategory())
                .date(r.getTransactionDate())
                .build();
    }
}
