package com.crediya.api.service;

import com.crediya.api.utils.JwtUtil;
import com.crediya.model.user.gateways.AuthInputPort;
import com.crediya.model.user.gateways.TokenInputPort;
import reactor.core.publisher.Mono;

import java.util.List;


public class AuthService  implements TokenInputPort {
    private final JwtUtil jwtUtil;
    private final AuthInputPort authInputPort;

    public AuthService(JwtUtil jwtUtil, AuthInputPort authInputPort) {
        this.jwtUtil = jwtUtil;
        this.authInputPort = authInputPort;
    }

    @Override
    public String generateToken(String subject, String role, List<String> permissions) {
        return jwtUtil.generateToken(subject, role, permissions);
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public Mono<String> authenticate(String email, String password) {
        return authInputPort.authenticate(email, password);
    }

}
