package com.erp.montfortuganda.auth.service;

import com.erp.montfortuganda.auth.dto.UserDTO;
import com.erp.montfortuganda.auth.entity.ErpRole;
import com.erp.montfortuganda.auth.entity.User;
import com.erp.montfortuganda.auth.repository.ErpRoleRepository;
import com.erp.montfortuganda.auth.repository.UserRepository;
import com.erp.montfortuganda.school.dto.BranchDTO;
import com.erp.montfortuganda.school.entity.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final ErpRoleRepository roleRepository;

    public UserServiceImpl(
            UserRepository userRepository,
            BranchRepository branchRepository,
            PasswordEncoder passwordEncoder,
            ErpRoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public UserDTO createUser(UserDTO dto) {

        String username = normalizeUsername(dto.getUsername());

        if (username == null) {
            throw new IllegalArgumentException("Username is required");
        }

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException(
                    "Username is already taken"
            );
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(resolveRoleCode(dto));
        user.setIsActive(1);

        if (dto.getAssignedBranchId() != null) {
            Branch branch = branchRepository
                    .findById(dto.getAssignedBranchId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Branch not found: "
                                            + dto.getAssignedBranchId()
                            )
                    );

            user.setAssignedBranch(branch);
        }

        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    @Override
    public UserDTO updateUser(Integer id, UserDTO dto) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + id
                        )
                );

        if (dto.getUsername() != null
                && !dto.getUsername().isBlank()) {

            String normalizedUsername =
                    normalizeUsername(dto.getUsername());

            userRepository.findByUsername(normalizedUsername)
                    .filter(existing ->
                            !existing.getId().equals(user.getId())
                    )
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException(
                                "Username is already taken"
                        );
                    });

            user.setUsername(normalizedUsername);
        }

        if (dto.getRoleId() != null
                || dto.getRole() != null) {
            user.setRole(resolveRoleCode(dto));
        }

        if (dto.getPassword() != null
                && !dto.getPassword().isBlank()) {
            user.setPassword(
                    passwordEncoder.encode(dto.getPassword())
            );
        }

        if (dto.getIsActive() != null) {
            user.setIsActive(
                    Integer.valueOf(1).equals(dto.getIsActive())
                            ? 1
                            : 0
            );
        }

        if (dto.getAssignedBranchId() != null) {
            Branch branch = branchRepository
                    .findById(dto.getAssignedBranchId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Branch not found: "
                                            + dto.getAssignedBranchId()
                            )
                    );

            user.setAssignedBranch(branch);
        } else {
            user.setAssignedBranch(null);
        }

        User updatedUser = userRepository.save(user);
        return mapToDTO(updatedUser);
    }

    @Override
    public void softDeleteUser(Integer id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + id
                        )
                );

        user.setIsActive(0);
        userRepository.save(user);
    }

    private String resolveRoleCode(UserDTO dto) {

        String role;

        if (dto.getRoleId() != null) {
            ErpRole erpRole = roleRepository
                    .findById(dto.getRoleId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Role not found: "
                                            + dto.getRoleId()
                            )
                    );

            if (!Boolean.TRUE.equals(erpRole.getActive())) {
                throw new IllegalArgumentException(
                        "Selected role is inactive"
                );
            }

            role = erpRole.getRoleCode();
        } else {
            role = dto.getRole();
        }

        if (role == null || role.isBlank()) {
            role = "EMPLOYEE";
        }

        return normalizeRoleCode(role);
    }

    private String normalizeRoleCode(String role) {

        if (role == null || role.isBlank()) {
            return "EMPLOYEE";
        }

        String normalized = role.trim()
                .toUpperCase(Locale.ROOT)
                .replace(' ', '_')
                .replace('-', '_');

        while (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }

        return switch (normalized) {
            case "SUPER_USER" -> "SUPER_ADMIN";
            case "SCHOOL_ADMIN" -> "BRANCH_ADMIN";
            default -> normalized;
        };
    }

    private String normalizeUsername(String username) {

        if (username == null || username.isBlank()) {
            return null;
        }

        return username.trim();
    }

    private UserDTO mapToDTO(User user) {

        UserDTO dto = new UserDTO();

        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        dto.setIsActive(user.getIsActive());

        if (user.getAssignedBranch() != null) {
            Branch branch = user.getAssignedBranch();

            dto.setAssignedBranchId(branch.getBranchId());

            BranchDTO branchDTO = new BranchDTO();
            branchDTO.setBranchId(branch.getBranchId());
            branchDTO.setBranchName(branch.getBranchName());

            dto.setAssignedBranch(branchDTO);
        }

        return dto;
    }
}