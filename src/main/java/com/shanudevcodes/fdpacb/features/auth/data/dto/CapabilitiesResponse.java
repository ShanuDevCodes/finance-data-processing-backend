package com.shanudevcodes.fdpacb.features.auth.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapabilitiesResponse {
    private boolean canCreateRecords;
    private boolean canManageUsers;
    private boolean canFilterByUsers;
    private List<String> allowedFilters;
}
