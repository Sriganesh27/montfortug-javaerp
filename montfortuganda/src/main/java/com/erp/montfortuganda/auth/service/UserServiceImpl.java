package com.erp.montfortuganda.auth.service;

import com.erp.montfortuganda.auth.User;
import com.erp.montfortuganda.auth.repository.UserRepository;
import com.erp.montfortuganda.auth.dto.UserDTO;
import com.erp.montfortuganda.school.Branch;
import com.erp.montfortuganda.school.repository.BranchRepository;
import com.erp.montfortuganda.school.dto.BranchDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public UserDTO createUser(UserDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("Error: Username is already taken!");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        String role = dto.getRole();
        if ("Super User".equalsIgnoreCase(role)) {
            role = "SUPER_ADMIN";
        }
        user.setRole(role);
        user.setIsActive(1);

        if (dto.getAssignedBranchId() != null) {
            Branch branch = branchRepository.findById(dto.getAssignedBranchId().longValue())
                    .orElseThrow(() -> new IllegalArgumentException("Branch not found"));
            user.setAssignedBranch(branch);
        }

        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    @Override
    public UserDTO updateUser(Integer id, UserDTO dto) {
        // FIXED: Using raw id because UserRepository expects Integer
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String role = dto.getRole();
        if ("Super User".equalsIgnoreCase(role)) {
            role = "SUPER_ADMIN";
        }
        user.setRole(role);

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getAssignedBranchId() != null) {
            Branch branch = branchRepository.findById(dto.getAssignedBranchId().longValue())
                    .orElseThrow(() -> new IllegalArgumentException("Branch not found"));
            user.setAssignedBranch(branch);
        } else {
            user.setAssignedBranch(null);
        }

        User updatedUser = userRepository.save(user);
        return mapToDTO(updatedUser);
    }

    @Override
    public void softDeleteUser(Integer id) {
        // FIXED: Using raw id because UserRepository expects Integer
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsActive(0);
        userRepository.save(user);
    }

    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId() != null ? user.getId().intValue() : null);
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        dto.setIsActive(user.getIsActive());

        if (user.getAssignedBranch() != null) {
            dto.setAssignedBranchId(user.getAssignedBranch().getBranchId() != null ? user.getAssignedBranch().getBranchId().intValue() : null);
            BranchDTO branchDTO = new BranchDTO();
            branchDTO.setBranchId(user.getAssignedBranch().getBranchId() != null ? user.getAssignedBranch().getBranchId().intValue() : null);
            branchDTO.setBranchName(user.getAssignedBranch().getBranchName());
            dto.setAssignedBranch(branchDTO);
        }
        return dto;
    }
}