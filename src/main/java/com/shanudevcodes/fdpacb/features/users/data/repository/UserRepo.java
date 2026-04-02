package com.shanudevcodes.fdpacb.features.users.data.repository;

import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import com.shanudevcodes.fdpacb.security.rbac.role.Role;
import com.shanudevcodes.fdpacb.security.rbac.role.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<UserModel, UUID> {
    Optional<UserModel> findByEmail(String email);

    @Query("""
    SELECT u FROM UserModel u
    WHERE (:role IS NULL OR :role MEMBER OF u.roles)
      AND (:status IS NULL OR u.status = :status)
      AND (:analystId IS NULL OR u.assignedAnalyst.id = :analystId)
""")
    Page<UserModel> findAllWithFilters(
            @Param("role") Role role,
            @Param("status") Status status,
            @Param("analystId") UUID analystId,
            Pageable pageable
    );

    List<UserModel> findUserModelByAssignedAnalyst_Id(UUID assignedAnalystId);
}