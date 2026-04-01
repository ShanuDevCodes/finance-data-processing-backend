package com.shanudevcodes.fdpacb.features.records.domain.service;

import com.shanudevcodes.fdpacb.features.records.data.dto.CreateRecordRequest;
import com.shanudevcodes.fdpacb.features.records.data.dto.UpdateRecordRequest;
import com.shanudevcodes.fdpacb.features.records.data.entity.RecordsModel;
import com.shanudevcodes.fdpacb.features.records.data.enums.Category;
import com.shanudevcodes.fdpacb.features.records.data.enums.PaymentMethod;
import com.shanudevcodes.fdpacb.features.records.data.enums.RecordType;
import com.shanudevcodes.fdpacb.features.records.data.repository.RecordsRepo;
import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import com.shanudevcodes.fdpacb.features.users.data.repository.UserRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public class RecordService {
    private final RecordsRepo recordsRepo;
    RecordService(
            RecordsRepo recordsRepo,
            UserRepo userRepo
    ){
        this.recordsRepo = recordsRepo;
    }

    public void createRecord(CreateRecordRequest request, UserModel user){
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
    }

    public void updateRecord(UUID recordId, UpdateRecordRequest request, UserModel user){
        Optional<RecordsModel> record = recordsRepo.findByIdAndUserIdAndIsDeletedFalse(recordId,user.getId());
        if (record.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found");
        }
        if (request.getAmount() != null) {
            record.get().setAmount(request.getAmount());
        }
        RecordType type = record.get().getType();
        Category category = record.get().getCategory();
        if (request.getType() != null) {
            type = RecordType.valueOf(request.getType().toUpperCase());
            record.get().setType(type);
        }
        if (request.getCategory() != null) {
            category = Category.valueOf(request.getCategory().toUpperCase());
            record.get().setCategory(category);
        }
        if (!category.getType().equals(type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category does not match record type");
        }
        record.get().setType(type);
        record.get().setCategory(category);
        if (request.getPaymentMethod() != null) {
            record.get().setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()));
        }
        if (request.getNote() != null) {
            record.get().setNote(request.getNote());
        }
        if (request.getIsRecurring() != null) {
            record.get().setIsRecurring(request.getIsRecurring());
        }
        if (request.getCurrency() != null) {
            record.get().setCurrency(request.getCurrency());
        }
        recordsRepo.save(record.get());
    }

    public void deleteRecord(UUID recordId, UserModel user){
        Optional<RecordsModel> record = recordsRepo.findByIdAndUserIdAndIsDeletedFalse(recordId, user.getId());
        if (record.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found");
        }
        record.get().setIsDeleted(true);
        recordsRepo.save(record.get());
    }

    public Page<RecordsModel> getAllRecords(
            UserModel user,
            int page,
            int size,
            String type,
            String category
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        RecordType recordType = null;
        Category recordCategory = null;
        if (type != null) {
            recordType = RecordType.valueOf(type.toUpperCase());
        }
        if (category != null) {
            recordCategory = Category.valueOf(category.toUpperCase());
        }
        return recordsRepo.findAllByFilters(
                user.getId(),
                recordType,
                recordCategory,
                pageable
        );
    }
}
