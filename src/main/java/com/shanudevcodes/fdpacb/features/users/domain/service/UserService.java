package com.shanudevcodes.fdpacb.features.users.domain.service;

import com.shanudevcodes.fdpacb.common.util.HashPassEncoder;
import com.shanudevcodes.fdpacb.features.users.data.entity.UserModel;
import com.shanudevcodes.fdpacb.features.users.data.repository.UserRepo;
import com.shanudevcodes.fdpacb.security.rbac.role.Role;
import com.shanudevcodes.fdpacb.security.rbac.role.Status;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final HashPassEncoder hashPassEncoder;

    private UserModel getUser(UUID userId){
        return userRepo.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
                );
    }

    public Page<UserModel> getAllUsers(
            Pageable pageable,
            Role role,
            Status status,
            UUID analystId
    ) {
        int maxSize = 50;
        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                Math.min(pageable.getPageSize(), maxSize),
                pageable.getSort().isSorted()
                        ? pageable.getSort()
                        : Sort.by("createdAt").descending()
        );
        return userRepo.findAllWithFilters(role, status, analystId, safePageable);
    }

    @Transactional
    public UserModel toggleStatus(UUID userID) {
        UserModel user = getUser(userID);
        if (user.getStatus() == Status.ACTIVE) {
            user.setStatus(Status.INACTIVE);
        } else {
            user.setStatus(Status.ACTIVE);
        }
        return userRepo.save(user);
    }

    @Transactional
    public UserModel assignViewerToAnalyst(UUID viewer, UUID analystId) {
        if (viewer.equals(analystId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User cannot be assigned to themselves");
        }
        UserModel user = getUser(viewer);
        UserModel analyst = getUser(analystId);
        if (!analyst.getRoles().contains(Role.ANALYST)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assigned user is not an analyst");
        }
        user.setAssignedAnalyst(analyst);
        return userRepo.save(user);
    }

    @Transactional
    public UserModel changeRole(UUID userId, List<Role> roles){
        if (roles == null || roles.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Roles cannot be empty");
        }
        UserModel user = getUser(userId);
        user.setRoles(new HashSet<>(roles));
        return userRepo.save(user);
    }

    @Transactional
    public UserModel changeName(UserModel user, String name){
        user.setName(name);
        return userRepo.save(user);
    }

    @Transactional
    public UserModel changeEmail(UserModel user, String email){
        if (userRepo.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        user.setEmail(email);
        return userRepo.save(user);
    }

    @Transactional
    public UserModel changePassword(UserModel user, String password){
        if (hashPassEncoder.matches(password,user.getHashedPassword())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password cannot be the same as the old password");
        }
        String newHashedPassword = hashPassEncoder.encodePassword(password);
        user.setHashedPassword(newHashedPassword);
        return userRepo.save(user);
    }

    public List<UserModel> getAssignedViewers(UUID analystId) {
        return userRepo.findUserModelByAssignedAnalyst_Id(analystId);
    }
}
