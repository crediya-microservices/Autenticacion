package com.crediya.usecase.user;


import com.crediya.model.role.gateways.RoleRepository;
import com.crediya.model.user.User;
import com.crediya.model.user.gateways.UserInputPort;
import com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class UserUseCase implements UserInputPort {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public Mono<User> saveUser(User user) {
        return validateUser(user)
                .then(userRepository.getUserByEmail(user.getEmail())
                        .hasElement()
                        .flatMap(exists -> {
                            if (exists) return Mono.error(new IllegalStateException("El correo ya está registrado"));
                            return roleRepository.getRoleByName(user.getRoleName())
                                    .switchIfEmpty(Mono.error(new IllegalArgumentException("El rol no existe")))
                                    .flatMap(role -> {
                                        user.setRoleName(role.getName());
                                        return userRepository.saveUser(user, role.getId());
                                    });
                        })
                );
    }

    @Override
    public Mono<User> updateUser(User user) {
        return validateUser(user)
                .then(roleRepository.getRoleByName(user.getRoleName())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("El rol no existe")))
                        .flatMap(role -> userRepository.updateUser(user, role.getId())
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Usuario no encontrado")))
                        )
                );
    }

    @Override
    public Flux<User> getAllUsers() {
        return userRepository.getAllUsers()
                .onErrorResume(e -> Flux.empty());
    }

    private Mono<Void> validateUser(User user) {
        if (!isCompleteFields(user)) {
            return Mono.error(new IllegalArgumentException("Todos los campos son obligatorios"));
        }
        if (!isValidEmail(user.getEmail())) {
            return Mono.error(new IllegalArgumentException("El formato del correo electrónico no es válido"));
        }
        if (isValidSalary(user.getBaseSalary())) {
            return Mono.error(new IllegalArgumentException("El salario base debe estar entre 0 y 15,000,000"));
        }
        return Mono.empty();
    }

    private boolean isCompleteFields(User user) {
        return user.getName() != null && user.getLastName() != null && user.getEmail() != null &&
                user.getBaseSalary() != null && user.getIdentityDocument() != null && user.getRoleName() != null;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email != null && email.matches(emailRegex);
    }

    private boolean isValidSalary(BigDecimal baseSalary) {
        try {
            double salary = Double.parseDouble(String.valueOf(baseSalary));
            return salary < 0 || salary > 15000000;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}