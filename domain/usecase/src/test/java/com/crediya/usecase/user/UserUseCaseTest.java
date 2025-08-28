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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    private UserUseCase userUseCase;

    private final User validUser = new User(
            "1",
            "Andres",
            "Gomez",
            "andres@example.com",
            LocalDate.of(1990, 1, 1),
            "Calle 123",
            "123456789",
            BigDecimal.valueOf(5_000_000),
            "123456789",
            "ADMIN"
    );

    @BeforeEach
    void setUp() {
        userUseCase = new UserUseCase(userRepository, roleRepository);
    }

    // ---------------- saveUser ------------------

    @Test
    void saveUser_emailAlreadyExists() {
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(true));

        StepVerifier.create(userUseCase.saveUser(validUser))
                .expectErrorMatches(e -> e instanceof IllegalStateException &&
                        e.getMessage().equals("El correo ya está registrado"))
                .verify();
    }

    @Test
    void saveUser_documentAlreadyExists() {
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(false));
        when(userRepository.findByIdentityDocument(validUser.getIdentityDocument())).thenReturn(Mono.just(validUser));

        StepVerifier.create(userUseCase.saveUser(validUser))
                .expectErrorMatches(e -> e instanceof IllegalStateException &&
                        e.getMessage().equals("El documento de identidad ya está registrado"))
                .verify();
    }

    @Test
    void saveUser_roleNotFound() {
        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(false));
        when(userRepository.findByIdentityDocument(validUser.getIdentityDocument())).thenReturn(Mono.empty());
        when(roleRepository.getRoleByName(validUser.getRoleName())).thenReturn(Mono.empty());

        StepVerifier.create(userUseCase.saveUser(validUser))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().equals("El rol no existe"))
                .verify();
    }

    @Test
    void saveUser_success() {
        Role role = new Role();
        role.setId(1L);
        role.setName("ADMIN");

        when(userRepository.existsByEmail(validUser.getEmail())).thenReturn(Mono.just(false));
        when(userRepository.findByIdentityDocument(validUser.getIdentityDocument())).thenReturn(Mono.empty());
        when(roleRepository.getRoleByName(validUser.getRoleName())).thenReturn(Mono.just(role));
        when(userRepository.saveUser(validUser, role.getId())).thenReturn(Mono.just(validUser));

        StepVerifier.create(userUseCase.saveUser(validUser))
                .expectNext(validUser)
                .verifyComplete();

        verify(userRepository).saveUser(validUser, role.getId());
    }

    // ---------------- validateUser ------------------

    @Test
    void saveUser_missingFields() {
        User invalidUser = new User(
                "1",
                "Andres",
                null, // falta apellido
                "andres@example.com",
                LocalDate.now(),
                "Calle 123",
                "123",
                BigDecimal.valueOf(1000),
                "123",
                "ADMIN"
        );
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));

        StepVerifier.create(userUseCase.saveUser(invalidUser))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().equals("Todos los campos son obligatorios"))
                .verify();
    }

    @Test
    void saveUser_invalidEmail() {
        User invalidUser = new User(
                "1",
                "Andres",
                "Gomez",
                "invalid-email", // mal formato
                LocalDate.now(),
                "Calle 123",
                "123",
                BigDecimal.valueOf(1000),
                "123",
                "ADMIN"
        );
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));

        StepVerifier.create(userUseCase.saveUser(invalidUser))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().equals("El formato del correo electrónico no es válido"))
                .verify();
    }

    @Test
    void saveUser_invalidSalaryTooHigh() {
        User invalidUser = new User(
                "1",
                "Andres",
                "Gomez",
                "andres@example.com",
                LocalDate.now(),
                "Calle 123",
                "123",
                BigDecimal.valueOf(20_000_000), // > 15M
                "123",
                "ADMIN"
        );
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        StepVerifier.create(userUseCase.saveUser(invalidUser))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().equals("El salario base debe estar entre 0 y 15,000,000"))
                .verify();
    }

    @Test
    void saveUser_invalidSalaryNegative() {
        User invalidUser = new User(
                "1",
                "Andres",
                "Gomez",
                "andres@example.com",
                LocalDate.now(),
                "Calle 123",
                "123",
                BigDecimal.valueOf(-500), // negativo
                "123",
                "ADMIN"
        );
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        StepVerifier.create(userUseCase.saveUser(invalidUser))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().equals("El salario base debe estar entre 0 y 15,000,000"))
                .verify();
    }

    // ---------------- findByEmail ------------------

    @Test
    void findByEmail_success() {
        when(userRepository.getUserByEmail(validUser.getEmail())).thenReturn(Mono.just(validUser));

        StepVerifier.create(userUseCase.findByEmail(validUser.getEmail()))
                .expectNext(validUser)
                .verifyComplete();
    }

    @Test
    void findByEmail_userNotFound() {
        when(userRepository.getUserByEmail(validUser.getEmail())).thenReturn(Mono.empty());

        StepVerifier.create(userUseCase.findByEmail(validUser.getEmail()))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException &&
                        e.getMessage().equals("Usuario no encontrado"))
                .verify();
    }
}
