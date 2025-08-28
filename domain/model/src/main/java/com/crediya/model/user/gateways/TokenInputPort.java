package com.crediya.model.user.gateways;

import java.util.List;

public interface TokenInputPort {
    String generateToken(String subject, String role, List<String> permissions);
    boolean validateToken(String token);
}
