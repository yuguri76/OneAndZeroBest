package com.sparta.oneandzerobest.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class SignupRequest {
    private String username;
    private String password;
    private String email;
    private boolean isAdmin;
    private String adminToken;

    public SignupRequest(String username, String password, String email, boolean isAdmin,
        String adminToken) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.isAdmin = isAdmin;
        this.adminToken = adminToken;
    }
}
