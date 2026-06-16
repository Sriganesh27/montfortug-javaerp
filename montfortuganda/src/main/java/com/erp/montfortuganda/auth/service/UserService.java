package com.erp.montfortuganda.auth.service;

import com.erp.montfortuganda.auth.dto.UserDTO;
import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();
    UserDTO createUser(UserDTO userDTO);
    UserDTO updateUser(Integer id, UserDTO userDTO);
    void softDeleteUser(Integer id);
}