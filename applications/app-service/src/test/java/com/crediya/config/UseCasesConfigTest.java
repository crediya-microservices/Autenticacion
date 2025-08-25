package com.crediya.config;

import com.crediya.model.role.gateways.RoleRepository;
import com.crediya.usecase.user.UserUseCase;
import com.crediya.model.user.gateways.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(TestConfig.class)) {

            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    break;
                }
            }

            assertTrue(useCaseBeanFound,
                    "No beans ending with 'UseCase' were found");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {

        @Bean
        public UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }

        @Bean
        public RoleRepository roleRepository() {
            return Mockito.mock(RoleRepository.class);
        }


        @Bean
        public UserUseCase userUseCase(UserRepository userRepository, RoleRepository roleRepository) {
            return new UserUseCase(userRepository, roleRepository);
        }
    }
}
