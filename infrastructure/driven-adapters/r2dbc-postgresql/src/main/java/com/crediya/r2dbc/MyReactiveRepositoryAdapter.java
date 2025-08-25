package com.crediya.r2dbc;

import com.crediya.model.user.User;
import com.crediya.model.user.gateways.UserRepository;
import com.crediya.r2dbc.entity.UserEntity;
import com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Repository
public class MyReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        User,
        UserEntity,
        String,
        MyReactiveRepository
        > implements UserRepository {

    private final TransactionalOperator transactionalOperator;
    private final RoleReactiveRepository roleRepository;

    public MyReactiveRepositoryAdapter(
            MyReactiveRepository repository,
            RoleReactiveRepository roleRepository,
            ObjectMapper mapper,
            TransactionalOperator transactionalOperator
    ) {
        super(repository, mapper, d -> mapper.map(d, User.class));
        this.transactionalOperator = transactionalOperator;
        this.roleRepository = roleRepository;
    }

    @Override
    public Mono<User> getUserByEmail(String email) {
        return this.repository.findByEmail(email)
                .filter(Objects::nonNull)
                .map(entity -> mapper.map(entity, User.class))
                .onErrorMap(e -> new RuntimeException("Error al consultar usuario por email", e));
    }

    @Override
    public Mono<User> saveUser(User user, Long roleId) {
        UserEntity entity = mapper.map(user, UserEntity.class);
        entity.setRoleId(roleId);
        return this.repository.save(entity)
                .map(saved -> {
                    user.setId(String.valueOf(saved.getId()));
                    user.setRoleName(user.getRoleName());
                    return user;
                })
                .onErrorMap(e -> new RuntimeException("Error al guardar usuario", e))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Flux<User> getAllUsers() {
        return this.repository.findAll()
                .flatMap(entity ->
                        roleRepository.findById(String.valueOf(entity.getRoleId()))
                                .map(role -> {
                                    User user = mapper.map(entity, User.class);
                                    user.setRoleName(role.getName());
                                    return user;
                                })
                )
                .onErrorMap(e -> new RuntimeException("Error al consultar todos los usuarios", e));
    }

    @Override
    public Mono<User> updateUser(User user, Long roleId) {
        return this.repository.findByEmail(user.getEmail())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Usuario no encontrado, por favor verifique el email")))
                .flatMap(existingUser -> {
                    existingUser.setName(user.getName());
                    existingUser.setLastName(user.getLastName());
                    existingUser.setEmail(user.getEmail());
                    existingUser.setBaseSalary(user.getBaseSalary());
                    existingUser.setIdentityDocument(user.getIdentityDocument());
                    existingUser.setRoleId(roleId);
                    return this.repository.save(existingUser);
                })
                .flatMap(saved ->
                        roleRepository.findById(String.valueOf(saved.getRoleId()))
                                .map(role -> {
                                    User updated = mapper.map(saved, User.class);
                                    updated.setRoleName(role.getName());
                                    return updated;
                                })
                )
                .onErrorMap(e -> e instanceof IllegalArgumentException ? e : new RuntimeException("Error al actualizar usuario", e))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<User> findByIdentityDocument(String identityDocument) {
        return this.repository.findByIdentityDocument(identityDocument)
                .filter(Objects::nonNull)
                .map(entity -> mapper.map(entity, User.class));
    }
}