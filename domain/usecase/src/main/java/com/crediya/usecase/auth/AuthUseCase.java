package com.crediya.usecase.auth;

import com.crediya.model.permission.Permission;
import com.crediya.model.permission.gateways.PermissionRepository;
import com.crediya.model.user.User;
import com.crediya.model.user.gateways.AuthInputPort;
import com.crediya.model.user.gateways.PasswordEncoderInputPort;
import com.crediya.model.user.gateways.TokenInputPort;
import com.crediya.model.user.gateways.UserRepository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.logging.Logger;

public class AuthUseCase implements AuthInputPort {

    private final UserRepository userRepository;
    private final TokenInputPort tokenInputPort;
    private final PasswordEncoderInputPort passwordEncoderInputPort;
    private final PermissionRepository permissionRepository;
    private static final Logger logger = Logger.getLogger(AuthUseCase.class.getName());

    public AuthUseCase(UserRepository userRepository,
                       TokenInputPort tokenInputPort,
                       PasswordEncoderInputPort passwordEncoderInputPort,
                       PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.tokenInputPort = tokenInputPort;
        this.passwordEncoderInputPort = passwordEncoderInputPort;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public Mono<String> authenticate(String email, String password) {
        return findUser(email)
                .flatMap(user -> validatePassword(user, password))
                .flatMap(this::loadPermissionsAndGenerateToken)
                .doOnError(e -> logger.severe("Error en autenticación: " + e.getMessage()));
    }

    private Mono<User> findUser(String email) {
        return userRepository.getUserByEmail(email)
                .switchIfEmpty(Mono.error(new RuntimeException("Usuario no encontrado")))
                .doOnNext(user -> logger.info("Usuario encontrado: " + email));
    }

    private Mono<User> validatePassword(User user, String rawPassword) {
        if (user.getPassword() == null) {
            logger.info("Usuario sin contraseña: " + user.getEmail());
            return Mono.error(new RuntimeException("Usuario no tiene contraseña"));
        }
        if (!passwordEncoderInputPort.matches(rawPassword, user.getPassword())) {
            logger.info("Credenciales inválidas para usuario: " + user.getEmail());
            return Mono.error(new RuntimeException("Credenciales inválidas"));
        }
        logger.info("Contraseña válida para usuario: " + user.getEmail());
        return Mono.just(user);
    }

    private Mono<String> loadPermissionsAndGenerateToken(User user) {
        return permissionRepository.findByIdUser(Long.valueOf(user.getId()))
                .map(Permission::getName)
                .collectList()
                .doOnNext(perms -> logger.info("Permisos obtenidos para " + user.getEmail() + ": " + perms))
                .map(permissions -> buildToken(user, permissions));
    }

    private String buildToken(User user, List<String> permissions) {
        return tokenInputPort.generateToken(
                user.getEmail(),
                user.getRoleName(),
                permissions
        );
    }
}
