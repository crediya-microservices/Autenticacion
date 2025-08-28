package com.crediya.usecase.auth;

import com.crediya.model.user.User;
import com.crediya.model.user.gateways.AuthInputPort;
import com.crediya.model.user.gateways.PasswordEncoderInputPort;
import com.crediya.model.user.gateways.TokenInputPort;
import com.crediya.model.user.gateways.UserRepository;
import reactor.core.publisher.Mono;

public class AuthUseCase implements AuthInputPort {

    private final UserRepository userRepository;
    private final TokenInputPort tokenInputPort;
    private final PasswordEncoderInputPort passwordEncoderInputPort;

    public AuthUseCase(UserRepository userRepository, TokenInputPort tokenInputPort, PasswordEncoderInputPort passwordEncoderInputPort) {
        this.userRepository = userRepository;
        this.tokenInputPort = tokenInputPort;
        this.passwordEncoderInputPort = passwordEncoderInputPort;
    }

    @Override
    public Mono<String> authenticate(String email, String password) {
        Mono<User> user = userRepository.getUserByEmail(email);
        return user.flatMap(user -> {
            if(user == null) {
                return Mono.error(new RuntimeException("Usuario no encontrado"));
            }
            if(user.getPassword() == null) {
                return Mono.error(new RuntimeException("Usuario no tiene contraseña"));
            }
            if (passwordEncoderInputPort.matches(password, user.getPassword())) {
                return tokenInputPort.generateToken(user.getAddress(), user.getRoleName());
            } else {
                return Mono.error(new RuntimeException("Invalid credentials"));
            }
        });

    }
}
