package com.shanudevcodes.fdpacb.features.config.domain.service;

import com.shanudevcodes.fdpacb.features.records.data.enums.Category;
import com.shanudevcodes.fdpacb.features.records.data.enums.RecordStatus;
import com.shanudevcodes.fdpacb.features.records.data.enums.RecordType;
import com.shanudevcodes.fdpacb.security.rbac.role.Role;
import com.shanudevcodes.fdpacb.security.rbac.role.Status;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class ConfigService {
    public Map<String, Object> getMetadataEnums() {
        return Map.of(
                "categories", Category.values(),
                "recordTypes", RecordType.values(),
                "recordStatuses", RecordStatus.values(),
                "roles", Role.values(),
                "accountStatuses", Status.values()
        );
    }
}
