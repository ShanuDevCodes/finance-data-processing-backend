package com.shanudevcodes.fdpacb.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class HashPassEncoder {
    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
    public String encodePassword(String password){
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return bcrypt.encode(password);
    }
    public boolean matches(String password, String hashedPassword){
        if (password == null || hashedPassword == null) {
            return false;
        }
        return bcrypt.matches(password, hashedPassword);
    }
}
