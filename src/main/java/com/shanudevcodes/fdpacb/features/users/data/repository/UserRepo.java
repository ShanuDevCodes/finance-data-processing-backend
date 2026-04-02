package com.shanudevcodes.fdpacb.features.users.data.repository;

import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<UserModel, UUID> {
    Optional<UserModel> findByEmail(String email);

    List<UserModel> findUserModelByAssignedAnalyst_Id(UUID assignedAnalystId);
}