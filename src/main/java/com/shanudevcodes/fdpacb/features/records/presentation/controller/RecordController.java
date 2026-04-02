package com.shanudevcodes.fdpacb.features.records.presentation.controller;

import com.shanudevcodes.fdpacb.common.exception.dto.ApiResponse;
import com.shanudevcodes.fdpacb.features.records.data.dto.CreateRecordRequest;
import com.shanudevcodes.fdpacb.features.records.data.dto.RecordCreatedResponse;
import com.shanudevcodes.fdpacb.features.records.data.dto.UpdateRecordRequest;
import com.shanudevcodes.fdpacb.features.records.data.entity.RecordsModel;
import com.shanudevcodes.fdpacb.features.records.domain.service.RecordService;
import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/records")
@RequiredArgsConstructor
public class RecordController {
    private final RecordService recordService;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<RecordCreatedResponse>> create(
            @Valid @RequestBody CreateRecordRequest request,
            @PathVariable UUID userId
    ){
        RecordCreatedResponse response = recordService.createRecord(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<RecordCreatedResponse>builder()
                        .status("success")
                        .message("Record created successfully")
                        .data(response)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/{recordId}")
    public ResponseEntity<ApiResponse<String>> update(
            @PathVariable UUID recordId,
            @Valid @RequestBody UpdateRecordRequest request
    ){
        recordService.updateRecord(recordId, request);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status("success")
                        .message("Record updated successfully")
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RecordsModel>>> getAll(
            @AuthenticationPrincipal UserModel user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<UUID> assigned_userid
    ) {
        Page<RecordsModel> records = recordService.getAllRecords(
                user.getId(), page, size, type, category, assigned_userid
        );

        return ResponseEntity.ok(
                ApiResponse.<Page<RecordsModel>>builder()
                        .status("success")
                        .message("Records fetched successfully")
                        .data(records)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{recordId}")
    public ResponseEntity<ApiResponse<String>> delete(
            @PathVariable UUID recordId
    ) {
        recordService.deleteRecord(recordId);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status("success")
                        .message("Record deleted")
                        .build()
        );
    }

}
