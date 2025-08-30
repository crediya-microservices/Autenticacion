package com.crediya.usecase.auth;

import com.crediya.model.permission.Permission;
import com.crediya.model.permission.gateways.PermissionRepository;
import com.crediya.model.user.User;
import com.crediya.model.user.gateways.PasswordEncoderInputPort;
import com.crediya.model.user.gateways.TokenInputPort;
import com.crediya.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;

class AuthUseCaseTest {

    private UserRepository userRepository;
    private TokenInputPort tokenInputPort;
    private PasswordEncoderInputPort passwordEncoderInputPort;
    private PermissionRepository permissionRepository;
    private AuthUseCase authUseCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        tokenInputPort = mock(TokenInputPort.class);
        passwordEncoderInputPort = mock(PasswordEncoderInputPort.class);
        permissionRepository = mock(PermissionRepository.class);

        authUseCase = new AuthUseCase(
                userRepository,
                tokenInputPort,
                passwordEncoderInputPort,
                permissionRepository
        );
    }

    @Test
    void authenticateSuccess() {
        User user = new User();
        user.setId("1");
        user.setEmail("test@mail.com");
        user.setPassword("encodedPassword");
        user.setRoleName("ADMIN");

        when(userRepository.getUserByEmail("test@mail.com"))
                .thenReturn(Mono.just(user));
        when(passwordEncoderInputPort.matches("rawPassword", "encodedPassword"))
                .thenReturn(true);
        when(permissionRepository.findByIdUser(1L))
                .thenReturn(Flux.just(new Permission(1,"READ","Descripcion"), new Permission(2,"WRITE","Descripcion")));
        when(tokenInputPort.generateToken("test@mail.com", "ADMIN", List.of("READ", "WRITE")))
                .thenReturn("mockToken");

        StepVerifier.create(authUseCase.authenticate("test@mail.com", "rawPassword"))
                .expectNext("mockToken")
                .verifyComplete();

        verify(userRepository).getUserByEmail("test@mail.com");
        verify(passwordEncoderInputPort).matches("rawPassword", "encodedPassword");
        verify(permissionRepository).findByIdUser(1L);
        verify(tokenInputPort).generateToken("test@mail.com", "ADMIN", List.of("READ", "WRITE"));
    }

    @Test
    void authenticateUserNotFound() {
        when(userRepository.getUserByEmail("notfound@mail.com"))
                .thenReturn(Mono.empty());

        StepVerifier.create(authUseCase.authenticate("notfound@mail.com", "password"))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Usuario o contraseña inválidos"))
                .verify();

        verify(userRepository).getUserByEmail("notfound@mail.com");
        verifyNoInteractions(passwordEncoderInputPort, permissionRepository, tokenInputPort);
    }

    @Test
    void authenticateUserWithoutPassword() {
        User user = new User();
        user.setId("2");
        user.setEmail("nopass@mail.com");
        user.setPassword(null);

        when(userRepository.getUserByEmail("nopass@mail.com"))
                .thenReturn(Mono.just(user));

        StepVerifier.create(authUseCase.authenticate("nopass@mail.com", "any"))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Usuario no tiene contraseña"))
                .verify();

        verify(userRepository).getUserByEmail("nopass@mail.com");
        verifyNoInteractions(permissionRepository, tokenInputPort);
    }

    @Test
    void authenticateInvalidPassword() {
        User user = new User();
        user.setId("3");
        user.setEmail("wrongpass@mail.com");
        user.setPassword("encodedPassword");

        when(userRepository.getUserByEmail("wrongpass@mail.com"))
                .thenReturn(Mono.just(user));
        when(passwordEncoderInputPort.matches("badPassword", "encodedPassword"))
                .thenReturn(false);

        StepVerifier.create(authUseCase.authenticate("wrongpass@mail.com", "badPassword"))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Usuario o contraseña inválidos"))
                .verify();

        verify(userRepository).getUserByEmail("wrongpass@mail.com");
        verify(passwordEncoderInputPort).matches("badPassword", "encodedPassword");
        verifyNoInteractions(permissionRepository, tokenInputPort);
    }

    @Test
    void authenticateBlockedUser() {
        String email = "blocked@mail.com";

        User user = new User();
        user.setId("10");
        user.setEmail(email);
        user.setPassword("encodedPassword");

        when(userRepository.getUserByEmail(email))
                .thenReturn(Mono.just(user));
        when(passwordEncoderInputPort.matches("badPassword", "encodedPassword"))
                .thenReturn(false);

        for (int i = 0; i < 3; i++) {
            StepVerifier.create(authUseCase.authenticate(email, "badPassword"))
                    .expectError(RuntimeException.class)
                    .verify();
        }

        StepVerifier.create(authUseCase.authenticate(email, "badPassword"))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Demasiados intentos fallidos. Intente nuevamente en 1 minuto."))
                .verify();

        verify(userRepository, atLeast(3)).getUserByEmail(email);
        verify(passwordEncoderInputPort, atLeast(3)).matches("badPassword", "encodedPassword");
    }


    @Test
    void authenticateResetsAttemptsOnSuccess() {
        String email = "reset@mail.com";

        User user = new User();
        user.setId("12");
        user.setEmail(email);
        user.setPassword("encodedPassword");
        user.setRoleName("USER");

        when(userRepository.getUserByEmail(email))
                .thenReturn(Mono.just(user));
        when(passwordEncoderInputPort.matches("wrong", "encodedPassword"))
                .thenReturn(false);
        when(passwordEncoderInputPort.matches("good", "encodedPassword"))
                .thenReturn(true);
        when(permissionRepository.findByIdUser(12L))
                .thenReturn(Flux.just(new Permission(1,"READ","desc")));
        when(tokenInputPort.generateToken(email, "USER", List.of("READ")))
                .thenReturn("validToken");

        StepVerifier.create(authUseCase.authenticate(email, "wrong"))
                .expectError(RuntimeException.class)
                .verify();

        StepVerifier.create(authUseCase.authenticate(email, "good"))
                .expectNext("validToken")
                .verifyComplete();

        StepVerifier.create(authUseCase.authenticate(email, "good"))
                .expectNext("validToken")
                .verifyComplete();

        verify(tokenInputPort, atLeast(2)).generateToken(email, "USER", List.of("READ"));
    }

}
