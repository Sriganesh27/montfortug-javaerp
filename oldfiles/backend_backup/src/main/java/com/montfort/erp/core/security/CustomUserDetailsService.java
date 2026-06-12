package com.montfort.erp.core.security;

import com.montfort.erp.modules.auth.entity.User;
import com.montfort.erp.modules.finance.entity.StudentAccount;
import com.montfort.erp.modules.auth.repository.UserRepository;
import com.montfort.erp.modules.finance.repository.StudentAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentAccountRepository studentAccountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByUsernameAndIsActive(username, 1);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    new ArrayList<>() // Add roles here if needed
            );
        }

        Optional<StudentAccount> parentOpt = studentAccountRepository.findByUsernameAndIsActive(username, 1);
        if (parentOpt.isPresent()) {
            StudentAccount parent = parentOpt.get();
            return new org.springframework.security.core.userdetails.User(
                    parent.getUsername(),
                    parent.getPassword(),
                    new ArrayList<>()
            );
        }

        throw new UsernameNotFoundException("User not found or inactive: " + username);
    }
}

