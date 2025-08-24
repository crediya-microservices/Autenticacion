package com.crediya.usecase.user;


import com.crediya.model.user.User;
import com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class UserUseCase {

    private final UserRepository userRepository;

    public Mono<User> saveUser(User user) {
        if (!isCompleteFields(user)) {
            return Mono.error(new IllegalArgumentException("Todos los campos son obligatorios"));
        }
        if (!isValidEmail(user.getEmail())) {
            return Mono.error(new IllegalArgumentException("El formato del correo electrónico no es válido"));
        }
        if (isValidSalary(user.getBaseSalary())) {
            return Mono.error(new IllegalArgumentException("El salario base debe estar entre 0 y 15,000,000"));
        }
        return userRepository.getUserByEmail(user.getEmail())
                .hasElement()
                .flatMap(exists -> {
                    if (exists) return Mono.error(new IllegalStateException("El correo ya está registrado"));
                    return userRepository.saveUser(user);
                });
    }

    private Boolean isCompleteFields(User user) {
        return user.getName() != null && user.getLastName() != null && user.getEmail() != null &&
                user.getBaseSalary() != null;
    }

    private Boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email != null && email.matches(emailRegex);
    }

    private Boolean isValidSalary(BigDecimal baseSalary) {
        try {
            double salary = Double.parseDouble(String.valueOf(baseSalary));
            return salary < 0 || salary > 15000000;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}