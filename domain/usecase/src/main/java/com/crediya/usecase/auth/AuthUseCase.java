package com.crediya.usecase.auth;

import com.crediya.model.permission.Permission;
import com.crediya.model.permission.gateways.PermissionRepository;
import com.crediya.model.user.User;
import com.crediya.model.user.gateways.AuthInputPort;
import com.crediya.model.user.gateways.PasswordEncoderInputPort;
import com.crediya.model.user.gateways.TokenInputPort;
import com.crediya.model.user.gateways.UserRepository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class AuthUseCase implements AuthInputPort {

    private final UserRepository userRepository;
    private final TokenInputPort tokenInputPort;
    private final PasswordEncoderInputPort passwordEncoderInputPort;
    private final PermissionRepository permissionRepository;

    private static final Logger logger = Logger.getLogger(AuthUseCase.class.getName());

    private static final String INVALID_CREDENTIALS_MSG = "Usuario o contraseña inválidos";
    private static final String BLOCKED_MSG = "Demasiados intentos fallidos. Intente nuevamente en 1 minuto.";

    private static final int MAX_ATTEMPTS = 3;
    private static final long BLOCK_TIME_MS = 60_000;

    // Mapa en memoria para registrar intentos: email -> LoginAttempt
    private final Map<String, LoginAttempt> attemptsCache = new ConcurrentHashMap<>();

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
        if (isBlocked(email)) {
            logger.warning("Usuario bloqueado por intentos fallidos: " + email);
            return Mono.error(new RuntimeException(BLOCKED_MSG));
        }

        return findUser(email)
                .flatMap(user -> validatePassword(user, password))
                .flatMap(this::loadPermissionsAndGenerateToken)
                .doOnSuccess(token -> resetAttempts(email))
                .doOnError(e -> registerFailedAttempt(email));
    }

    private Mono<User> findUser(String email) {
        return userRepository.getUserByEmail(email)
                .switchIfEmpty(Mono.error(new RuntimeException(INVALID_CREDENTIALS_MSG)))
                .doOnNext(user -> logger.info("Usuario encontrado: " + email));
    }

    private Mono<User> validatePassword(User user, String rawPassword) {
        if (user.getPassword() == null) {
            logger.info("Usuario sin contraseña: " + user.getEmail());
            return Mono.error(new RuntimeException("Usuario no tiene contraseña"));
        }
        if (!passwordEncoderInputPort.matches(rawPassword, user.getPassword())) {
            logger.info("Credenciales inválidas para usuario: " + user.getEmail());
            return Mono.error(new RuntimeException(INVALID_CREDENTIALS_MSG));
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

    private void registerFailedAttempt(String email) {
        attemptsCache.compute(email, (key, attempt) -> {
            if (attempt == null) {
                return new LoginAttempt(1, Instant.now().toEpochMilli());
            }
            int newAttempts = attempt.attempts + 1;
            long now = Instant.now().toEpochMilli();

            if (newAttempts >= MAX_ATTEMPTS) {
                logger.warning("Usuario bloqueado por superar intentos: " + email);
                return new LoginAttempt(newAttempts, now);
            }
            return new LoginAttempt(newAttempts, attempt.firstAttemptTime);
        });
    }

    private boolean isBlocked(String email) {
        LoginAttempt attempt = attemptsCache.get(email);
        if (attempt == null) return false;

        long now = Instant.now().toEpochMilli();

        if (now - attempt.firstAttemptTime > BLOCK_TIME_MS) {
            resetAttempts(email);
            return false;
        }

        return attempt.attempts >= MAX_ATTEMPTS;
    }

    private void resetAttempts(String email) {
        attemptsCache.remove(email);
    }

    private record LoginAttempt(int attempts, long firstAttemptTime) {}
}
