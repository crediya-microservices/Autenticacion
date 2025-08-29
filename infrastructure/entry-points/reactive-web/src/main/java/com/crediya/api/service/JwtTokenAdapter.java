package com.crediya.api.service;

import com.crediya.api.security.JwtUtil;
import com.crediya.model.user.gateways.TokenInputPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtTokenAdapter implements TokenInputPort {

    private final JwtUtil jwtUtil;

    public JwtTokenAdapter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public String generateToken(String subject, String role, List<String> permissions) {
        return jwtUtil.generateToken(subject, role, permissions);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
}
