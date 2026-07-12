package com.tandiantong.security.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    public PasswordService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String hash(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    public boolean matches(String plainPassword, String passwordHash) {
        return passwordEncoder.matches(plainPassword, passwordHash);
    }
}
