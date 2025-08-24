package com.crediya.r2dbc;

import com.crediya.model.user.User;
import com.crediya.model.user.gateways.UserRepository;
import com.crediya.r2dbc.entity.UserEntity;
import com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
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

    public MyReactiveRepositoryAdapter(MyReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator) {
        super(repository, mapper, d -> mapper.map(d, User.class));
        this.transactionalOperator = transactionalOperator;
    }

  @Override
  public Mono<User> getUserByEmail(String email) {
      return this.repository.findByEmail(email)
              .filter(Objects::nonNull)
              .map(entity -> mapper.map(entity, User.class))
              .onErrorMap(e -> new RuntimeException("Error al consultar usuario por email", e));
  }

  @Override
  public Mono<User> saveUser(User user) {
      return this.repository.save(mapper.map(user, UserEntity.class))
              .map(entity -> mapper.map(entity, User.class))
              .onErrorMap(e -> new RuntimeException("Error al guardar usuario", e))
              .as(transactionalOperator::transactional);
  }
}
