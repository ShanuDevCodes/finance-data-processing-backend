package com.shanudevcodes.fdpacb.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class HashPassEncoder {
    private BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
    public String encodePassword(String password){
        try {
            return bcrypt.encode(password);
        } catch (Exception e) {
            return null;
        }
    }
    public boolean matches(String password, String hashedPassword){
        try {
            return bcrypt.matches(password, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
