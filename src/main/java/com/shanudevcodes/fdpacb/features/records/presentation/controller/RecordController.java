package com.shanudevcodes.fdpacb.features.records.presentation.controller;

import com.shanudevcodes.fdpacb.common.exception.dto.ApiResponse;
import com.shanudevcodes.fdpacb.features.records.data.dto.CreateRecordRequest;
import com.shanudevcodes.fdpacb.features.records.data.dto.UpdateRecordRequest;
import com.shanudevcodes.fdpacb.features.records.data.entity.RecordsModel;
import com.shanudevcodes.fdpacb.features.records.domain.service.RecordService;
import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/records")
public class RecordController {
    private final RecordService recordService;
    RecordController(RecordService recordService){
        this.recordService = recordService;
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<String>> create(
            @Valid @RequestBody CreateRecordRequest request,
            @AuthenticationPrincipal UserModel user
    ){
        recordService.createRecord(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<String>builder()
                        .status("success")
                        .message("Record created successfully")
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/{recordId}")
    public ResponseEntity<ApiResponse<String>> update(
            @PathVariable UUID recordId,
            @Valid @RequestBody UpdateRecordRequest request,
            @AuthenticationPrincipal UserModel user
    ){
        recordService.updateRecord(recordId, request, user);
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
            @RequestParam(required = false) String category
    ) {
        Page<RecordsModel> records = recordService.getAllRecords(
                user, page, size, type, category
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
            @PathVariable UUID recordId,
            @AuthenticationPrincipal UserModel user
    ) {
        recordService.deleteRecord(recordId,user);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .status("success")
                        .message("Record deleted")
                        .build()
        );
    }

}
