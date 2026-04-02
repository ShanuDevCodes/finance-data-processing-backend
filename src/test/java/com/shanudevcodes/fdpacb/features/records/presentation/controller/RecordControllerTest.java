package com.shanudevcodes.fdpacb.features.records.presentation.controller;

import com.shanudevcodes.fdpacb.common.exception.dto.ApiResponse;
import com.shanudevcodes.fdpacb.features.records.data.dto.CreateRecordRequest;
import com.shanudevcodes.fdpacb.features.records.domain.service.RecordService;
import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordControllerTest {

    @Mock
    private RecordService recordService;

    @InjectMocks
    private RecordController recordController;

    private UserModel mockUser;
    private CreateRecordRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockUser = UserModel.builder().id(UUID.randomUUID()).build();
        mockRequest = new CreateRecordRequest();
    }

    @Test
    void createRecord_ReturnsCreated() {
        UUID targetUserId = UUID.randomUUID();
        ResponseEntity<?> response = recordController.create(mockRequest, targetUserId);
        
        assertEquals(201, response.getStatusCode().value());
        verify(recordService).createRecord(mockRequest, targetUserId);
    }

    @Test
    void getAllRecords_ReturnsOk() {
        when(recordService.getAllRecords(any(), eq(0), eq(10), eq(null), eq(null), eq(null))).thenReturn(Page.empty());

        ResponseEntity<ApiResponse<Page<com.shanudevcodes.fdpacb.features.records.data.entity.RecordsModel>>> response = 
                recordController.getAll(mockUser, 0, 10, null, null, null);
        
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void deleteRecord_DelegatesToService() {
        UUID recordId = UUID.randomUUID();

        ResponseEntity<?> response = recordController.delete(recordId);
        
        assertEquals(200, response.getStatusCode().value());
        verify(recordService).deleteRecord(recordId);
    }
}
