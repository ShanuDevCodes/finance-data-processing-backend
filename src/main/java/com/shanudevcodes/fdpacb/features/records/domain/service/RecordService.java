package com.shanudevcodes.fdpacb.features.records.domain.service;

import com.shanudevcodes.fdpacb.features.records.data.dto.CreateRecordRequest;
import com.shanudevcodes.fdpacb.features.records.data.dto.RecordCreatedResponse;
import com.shanudevcodes.fdpacb.features.records.data.dto.UpdateRecordRequest;
import com.shanudevcodes.fdpacb.features.records.data.entity.RecordsModel;
import com.shanudevcodes.fdpacb.features.records.data.enums.Category;
import com.shanudevcodes.fdpacb.features.records.data.enums.PaymentMethod;
import com.shanudevcodes.fdpacb.features.records.data.enums.RecordType;
import com.shanudevcodes.fdpacb.features.records.data.repository.RecordsRepo;
import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import com.shanudevcodes.fdpacb.features.users.data.repository.UserRepo;
import com.shanudevcodes.fdpacb.security.rbac.role.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordService {
    private final RecordsRepo recordsRepo;
    private final UserRepo userRepo;

    @Transactional
    public RecordCreatedResponse createRecord(CreateRecordRequest request, UUID userID){
        UserModel user = userRepo.findById(userID).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        RecordType type = RecordType.valueOf(request.getType().toUpperCase());
        PaymentMethod paymentMethod = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        Category category = Category.valueOf(request.getCategory().toUpperCase());
        if (!category.getType().equals(type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category does not match record type");
        }
        RecordsModel newRecord = RecordsModel.builder()
                .amount(request.getAmount())
                .type(type)
                .category(category)
                .note(request.getNote())
                .paymentMethod(paymentMethod)
                .isRecurring(Boolean.TRUE.equals(request.getIsRecurring()))
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .user(user)
                .build();
        recordsRepo.save(newRecord);
        return RecordCreatedResponse.builder()
                .id(newRecord.getId())
                .amount(newRecord.getAmount())
                .type(newRecord.getType().name())
                .category(newRecord.getCategory().name())
                .transactionDate(newRecord.getTransactionDate())
                .status(newRecord.getStatus().name())
                .build();

    }

    @Transactional
    public void updateRecord(UUID recordId, UpdateRecordRequest request){
        RecordsModel record = recordsRepo.findByIdAndIsDeletedFalse(recordId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found")
        );
        if (request.getAmount() != null) {
            record.setAmount(request.getAmount());
        }
        RecordType type = record.getType();
        Category category = record.getCategory();
        if (request.getType() != null) {
            type = RecordType.valueOf(request.getType().toUpperCase());
            record.setType(type);
        }
        if (request.getCategory() != null) {
            category = Category.valueOf(request.getCategory().toUpperCase());
            record.setCategory(category);
        }
        if (!category.getType().equals(type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category does not match record type");
        }
        record.setType(type);
        record.setCategory(category);
        if (request.getPaymentMethod() != null) {
            record.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()));
        }
        if (request.getNote() != null) {
            record.setNote(request.getNote());
        }
        if (request.getIsRecurring() != null) {
            record.setIsRecurring(request.getIsRecurring());
        }
        if (request.getCurrency() != null) {
            record.setCurrency(request.getCurrency());
        }
        recordsRepo.save(record);
    }

    @Transactional
    public void deleteRecord(UUID recordId){
        RecordsModel record = recordsRepo.findByIdAndIsDeletedFalse(recordId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found")
        );
        record.setIsDeleted(true);
        recordsRepo.save(record);
    }

    @Transactional
    public Page<RecordsModel> getAllRecords(
            UUID userId,
            int page,
            int size,
            String type,
            String category,
            List<UUID> assignedUserIds
    ) {
        UserModel user = userRepo.findById(userId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
                );
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        RecordType recordType = null;
        Category recordCategory = null;
        if (type != null) {
            recordType = RecordType.valueOf(type.toUpperCase());
        }
        if (category != null) {
            recordCategory = Category.valueOf(category.toUpperCase());
        }
        if (user.getRoles().contains(Role.ADMIN)) {
            if (assignedUserIds != null && assignedUserIds.isEmpty()) {
                assignedUserIds = null;
            }
        } else if (user.getRoles().contains(Role.ANALYST)) {
            List<UUID> allowedUserIds = user.getAssignedUsers()
                    .stream()
                    .map(UserModel::getId)
                    .toList();
            if (assignedUserIds != null && !assignedUserIds.isEmpty()) {
                assignedUserIds = assignedUserIds.stream()
                        .filter(allowedUserIds::contains)
                        .toList();
            } else {
                assignedUserIds = allowedUserIds;
            }
        }
        return recordsRepo.findAllByFilters(
                assignedUserIds,
                recordType,
                recordCategory,
                pageable
        );
    }
}
