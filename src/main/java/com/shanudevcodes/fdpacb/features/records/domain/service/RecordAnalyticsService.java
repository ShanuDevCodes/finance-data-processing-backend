//package com.shanudevcodes.fdpacb.features.records.domain.service;
//
//import com.shanudevcodes.fdpacb.features.records.data.dto.DashboardResponse;
//import com.shanudevcodes.fdpacb.features.records.data.dto.RecentTransactionDto;
//import com.shanudevcodes.fdpacb.features.records.data.enums.Category;
//import com.shanudevcodes.fdpacb.features.records.data.enums.RecordStatus;
//import com.shanudevcodes.fdpacb.features.records.data.enums.RecordType;
//import com.shanudevcodes.fdpacb.features.records.data.repository.RecordsRepo;
//import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
//import com.shanudevcodes.fdpacb.features.users.data.repository.UserRepo;
//import com.shanudevcodes.fdpacb.security.rbac.role.Role;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//@Service
//public class RecordAnalyticsService {
//    private final RecordsRepo recordsRepo;
//    private final UserRepo userRepo;
//    RecordAnalyticsService(
//            RecordsRepo recordsRepo,
//            UserRepo userRepo
//    ){
//        this.recordsRepo = recordsRepo;
//        this.userRepo = userRepo;
//    }
//
//    public DashboardResponse getDashboard(UserModel user) {
//        List<UUID> userIds = getAccessibleUserIds(user);
//        BigDecimal income = recordsRepo.sumByType(userIds, RecordType.INCOME);
//        BigDecimal expense = recordsRepo.sumByType(userIds, RecordType.EXPENSE);
//        Map<Category, BigDecimal> categoryMap = mapToCategory(recordsRepo.categoryBreakdown(userIds));
//        Map<RecordType, BigDecimal> typeMap = mapToType(recordsRepo.typeBreakdown(userIds));
//        Map<RecordStatus, BigDecimal> statusMap = mapToStatus(recordsRepo.statusBreakdown(userIds));
//        List<RecentTransactionDto> recent = recordsRepo.findRecent(userIds)
//                .stream()
//                .map(this::toDto)
//                .toList();
//        return DashboardResponse.builder()
//                .totalIncome(income)
//                .totalExpense(expense)
//                .netBalance(income.subtract(expense))
//                .categoryBreakdown(categoryMap)
//                .typeBreakdown(typeMap)
//                .statusBreakdown(statusMap)
//                .recentTransactions(recent)
//                .build();
//    }
//
//    private List<UUID> getAccessibleUserIds(UserModel user) {
//        if (user.getRoles().contains(Role.ADMIN)) {
//            return userRepo.findAll();
//        }
//        if (user.getRoles().contains(Role.ANALYST)) {
//            List<UUID> ids = userRepo.findViewerIdsByAnalyst(user.getId());
//            ids.add(user.getId());
//            return ids;
//        }
//        return List.of(user.getId());
//    }
//
//    private Map<Category, BigDecimal> mapToCategory(List<Object[]> data) {
//        return data.stream()
//                .collect(Collectors.toMap(
//                        row -> (Category) row[0],
//                        row -> (BigDecimal) row[1]
//                ));
//    }
//
//    private Map<RecordType, BigDecimal> mapToType(List<Object[]> data) {
//        return data.stream()
//                .collect(Collectors.toMap(
//                        row -> (RecordType) row[0],
//                        row -> (BigDecimal) row[1]
//                ));
//    }
//
//    private Map<RecordStatus, BigDecimal> mapToStatus(List<Object[]> data) {
//        return data.stream()
//                .collect(Collectors.toMap(
//                        row -> (RecordStatus) row[0],
//                        row -> (BigDecimal) row[1]
//                ));
//    }
//
//    private RecentTransactionDto toDto(RecordsModel r) {
//        return RecentTransactionDto.builder()
//                .amount(r.getAmount())
//                .category(r.getCategory())
//                .date(r.getTransactionDate())
//                .build();
//    }
//}
