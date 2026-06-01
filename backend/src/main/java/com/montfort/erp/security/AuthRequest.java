package com.montfort.erp.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthRequest {
    private String username;
    private String password;
    private String role; 
    private String branch_id;
}
