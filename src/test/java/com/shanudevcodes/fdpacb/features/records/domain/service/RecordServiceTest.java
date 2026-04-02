package com.shanudevcodes.fdpacb.features.records.domain.service;

import com.shanudevcodes.fdpacb.features.records.data.entity.RecordsModel;
import com.shanudevcodes.fdpacb.features.records.data.repository.RecordsRepo;
import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import com.shanudevcodes.fdpacb.features.users.data.repository.UserRepo;
import com.shanudevcodes.fdpacb.security.rbac.role.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    private RecordsRepo recordsRepo;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private RecordService recordService;

    private UserModel analyst;
    private UserModel assignedViewer1;
    private UserModel assignedViewer2;
    private UUID unassignedViewerId;

    @BeforeEach
    void setUp() {
        assignedViewer1 = UserModel.builder().id(UUID.randomUUID()).roles(Set.of(Role.VIEWER)).build();
        assignedViewer2 = UserModel.builder().id(UUID.randomUUID()).roles(Set.of(Role.VIEWER)).build();
        unassignedViewerId = UUID.randomUUID();

        analyst = UserModel.builder()
                .id(UUID.randomUUID())
                .roles(Set.of(Role.ANALYST))
                .assignedUsers(Set.of(assignedViewer1, assignedViewer2))
                .build();
    }

    @Test
    void testAnalystDataScoping_RequestsAllAssignedData_GetsOnlyAssignedData() {
        when(userRepo.findById(analyst.getId())).thenReturn(Optional.of(analyst));
        when(recordsRepo.findAllByFilters(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(new RecordsModel())));

        recordService.getAllRecords(analyst.getId(), 0, 10, null, null, null);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UUID>> idCaptor = ArgumentCaptor.forClass(List.class);

        verify(recordsRepo).findAllByFilters(idCaptor.capture(), eq(null), eq(null), any(PageRequest.class));
        
        List<UUID> filteredIds = idCaptor.getValue();
        assertEquals(2, filteredIds.size());
        assertTrue(filteredIds.contains(assignedViewer1.getId()));
        assertTrue(filteredIds.contains(assignedViewer2.getId()));
    }

    @Test
    void testAnalystDataScoping_AttemptsToAccessUnassignedData_SecurityStripsUnassignedId() {
        when(userRepo.findById(analyst.getId())).thenReturn(Optional.of(analyst));
        when(recordsRepo.findAllByFilters(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        List<UUID> maliciousRequest = List.of(assignedViewer1.getId(), unassignedViewerId);
        recordService.getAllRecords(analyst.getId(), 0, 10, null, null, maliciousRequest);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UUID>> idCaptor = ArgumentCaptor.forClass(List.class);

        verify(recordsRepo).findAllByFilters(idCaptor.capture(), eq(null), eq(null), any(PageRequest.class));
        
        List<UUID> filteredIds = idCaptor.getValue();
        assertEquals(1, filteredIds.size());
        assertEquals(assignedViewer1.getId(), filteredIds.get(0));
    }
}
