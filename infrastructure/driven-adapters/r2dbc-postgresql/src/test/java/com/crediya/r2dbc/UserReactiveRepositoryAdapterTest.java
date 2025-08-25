package com.crediya.r2dbc;

import com.crediya.model.user.User;
import com.crediya.r2dbc.entity.RoleEntity;
import com.crediya.r2dbc.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserReactiveRepositoryAdapterTest {

    @InjectMocks
    UserReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    UserReactiveRepository repository;

    @Mock
    RoleReactiveRepository roleRepository;

    @Mock
    ObjectMapper mapper;

    @Mock
    TransactionalOperator transactionalOperator;

    @Test
    void mustGetUserByEmail() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("test@email.com");
        userEntity.setRoleId(1L);

        RoleEntity role = new RoleEntity();
        role.setId(1L);
        role.setName("ADMIN");

        User user = new User();
        user.setId("1");
        user.setEmail("test@email.com");
        user.setRoleName("ADMIN");

        when(repository.findByEmail("test@email.com")).thenReturn(Mono.just(userEntity));
        lenient().when(roleRepository.findById(String.valueOf(1L))).thenReturn(Mono.just(role));
        when(mapper.map(userEntity, User.class)).thenReturn(user);

        Mono<User> result = repositoryAdapter.getUserByEmail("test@email.com");

        StepVerifier.create(result)
                .expectNextMatches(found -> found.getEmail().equals("test@email.com")
                        && "ADMIN".equals(found.getRoleName()))
                .verifyComplete();
    }

    @Test
    void mustSaveUser() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setRoleName("ADMIN");

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("test@email.com");
        userEntity.setRoleId(1L);

        RoleEntity role = new RoleEntity();
        role.setId(1L);
        role.setName("ADMIN");

        when(mapper.map(user, UserEntity.class)).thenReturn(userEntity);
        when(repository.save(userEntity)).thenReturn(Mono.just(userEntity));
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(roleRepository.findById(String.valueOf(1L))).thenReturn(Mono.just(role));

        Mono<User> result = repositoryAdapter.saveUser(user, 1L);

        StepVerifier.create(result)
                .expectNextMatches(saved -> saved.getEmail().equals("test@email.com")
                        && "ADMIN".equals(saved.getRoleName()))
                .verifyComplete();
    }

    @Test
    void mustGetAllUsers() {
        UserEntity entity1 = new UserEntity();
        entity1.setId(1L);
        entity1.setEmail("a@test.com");
        entity1.setRoleId(1L);

        UserEntity entity2 = new UserEntity();
        entity2.setId(2L);
        entity2.setEmail("b@test.com");
        entity2.setRoleId(2L);

        RoleEntity role1 = new RoleEntity();
        role1.setId(1L);
        role1.setName("ADMIN");

        RoleEntity role2 = new RoleEntity();
        role2.setId(2L);
        role2.setName("USER");

        User user1 = new User();
        user1.setId("1");
        user1.setEmail("a@test.com");
        user1.setRoleName("ADMIN");

        User user2 = new User();
        user2.setId("2");
        user2.setEmail("b@test.com");
        user2.setRoleName("USER");

        when(repository.findAll()).thenReturn(Flux.fromIterable(Arrays.asList(entity1, entity2)));
        when(roleRepository.findById(String.valueOf(1L))).thenReturn(Mono.just(role1));
        when(roleRepository.findById(String.valueOf(2L))).thenReturn(Mono.just(role2));
        when(mapper.map(entity1, User.class)).thenReturn(user1);
        when(mapper.map(entity2, User.class)).thenReturn(user2);

        StepVerifier.create(repositoryAdapter.getAllUsers())
                .expectNextMatches(u -> u.getEmail().equals("a@test.com") && "ADMIN".equals(u.getRoleName()))
                .expectNextMatches(u -> u.getEmail().equals("b@test.com") && "USER".equals(u.getRoleName()))
                .verifyComplete();
    }

    @Test
    void mustUpdateUser() {
        User updated = new User();
        updated.setEmail("update@test.com");
        updated.setName("Updated");
        updated.setLastName("User");
        updated.setBaseSalary(new BigDecimal("5000"));
        updated.setRoleName("ADMIN");

        UserEntity existingEntity = new UserEntity();
        existingEntity.setId(1L);
        existingEntity.setEmail("update@test.com");

        UserEntity savedEntity = new UserEntity();
        savedEntity.setId(1L);
        savedEntity.setEmail("update@test.com");
        savedEntity.setRoleId(1L);

        RoleEntity role = new RoleEntity();
        role.setId(1L);
        role.setName("ADMIN");

        when(repository.findByEmail("update@test.com")).thenReturn(Mono.just(existingEntity));
        when(repository.save(any(UserEntity.class))).thenReturn(Mono.just(savedEntity));
        when(roleRepository.findById(String.valueOf(1L))).thenReturn(Mono.just(role));
        when(mapper.map(savedEntity, User.class)).thenReturn(updated);
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Mono<User> result = repositoryAdapter.updateUser(updated, 1L);

        StepVerifier.create(result)
                .expectNextMatches(u -> u.getEmail().equals("update@test.com") && "Updated".equals(u.getName())
                        && "ADMIN".equals(u.getRoleName()))
                .verifyComplete();
    }

    @Test
    void mustFailUpdateUserIfNotFound() {
        User updated = new User();
        updated.setEmail("notfound@test.com");

        when(repository.findByEmail("notfound@test.com")).thenReturn(Mono.empty());
        when(transactionalOperator.transactional(any(Mono.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(repositoryAdapter.updateUser(updated, 1L))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("Usuario no encontrado"))
                .verify();
    }

}