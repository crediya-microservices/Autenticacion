package com.crediya.usecase.user;

import com.crediya.model.role.Role;
import com.crediya.model.role.gateways.RoleRepository;
import com.crediya.model.user.User;
import com.crediya.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    private UserUseCase userUseCase;

    private final User validUser = User.builder()
            .name("Andres")
            .lastName("Gomez")
            .email("andres@example.com")
            .baseSalary(BigDecimal.valueOf(5_000_000))
            .identityDocument("123456789")
            .roleName("ADMIN")
            .build();

    @BeforeEach
    void setUp() {
        userUseCase = new UserUseCase(userRepository, roleRepository);
    }


    @Test
    void saveUser_emailAlreadyExists() {
        when(userRepository.getUserByEmail(validUser.getEmail())).thenReturn(Mono.just(validUser));

        StepVerifier.create(userUseCase.saveUser(validUser))
                .expectErrorMatches(e -> e instanceof IllegalStateException &&
                        e.getMessage().equals("El correo ya está registrado"))
                .verify();
    }

    @Test
    void saveUser_documentAlreadyExists() {
        when(userRepository.getUserByEmail(validUser.getEmail())).thenReturn(Mono.empty());
        when(userRepository.findByIdentityDocument(validUser.getIdentityDocument())).thenReturn(Mono.just(validUser));

        StepVerifier.create(userUseCase.saveUser(validUser))
                .expectErrorMatches(e -> e instanceof IllegalStateException &&
                        e.getMessage().equals("El documento de identidad ya está registrado"))
                .verify();
    }

    @Test
    void saveUser_roleNotFound() {
        when(userRepository.getUserByEmail(validUser.getEmail())).thenReturn(Mono.empty());
        when(userRepository.findByIdentityDocument(validUser.getIdentityDocument())).thenReturn(Mono.empty());
        when(roleRepository.getRoleByName(validUser.getRoleName())).thenReturn(Mono.empty());

        StepVerifier.create(userUseCase.saveUser(validUser))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().equals("El rol no existe"))
                .verify();
    }

    @Test
    void updateUser_roleNotFound() {
        when(roleRepository.getRoleByName(validUser.getRoleName())).thenReturn(Mono.empty());

        StepVerifier.create(userUseCase.updateUser(validUser))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().equals("El rol no existe"))
                .verify();
    }


    @Test
    void getAllUsers_successful() {
        when(userRepository.getAllUsers()).thenReturn(Flux.just(validUser));

        StepVerifier.create(userUseCase.getAllUsers())
                .expectNext(validUser)
                .verifyComplete();
    }

    @Test
    void getAllUsers_errorFallbackToEmpty() {
        when(userRepository.getAllUsers()).thenReturn(Flux.error(new RuntimeException("DB error")));

        StepVerifier.create(userUseCase.getAllUsers())
                .expectNextCount(0)
                .verifyComplete();
    }


}
