package com.crediya.r2dbc;

import com.crediya.model.user.User;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyReactiveRepositoryAdapterTest {

    @InjectMocks
    MyReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    MyReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    @Mock
    TransactionalOperator transactionalOperator;

    @Test
    void mustGetUserByEmail() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("test@email.com");

        User user = new User();
        user.setId("1");
        user.setEmail("test@email.com");

        when(repository.findByEmail("test@email.com"))
                .thenReturn(Mono.just(userEntity));
        when(mapper.map(userEntity, User.class)).thenReturn(user);

        Mono<User> result = repositoryAdapter.getUserByEmail("test@email.com");

        StepVerifier.create(result)
                .expectNextMatches(found -> found.getEmail().equals("test@email.com"))
                .verifyComplete();
    }

    @Test
    void mustSaveUser() {
        User user = new User();
        user.setId("1");
        user.setEmail("test@email.com");

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setEmail("test@email.com");

        when(mapper.map(user, UserEntity.class)).thenReturn(userEntity);
        when(repository.save(userEntity)).thenReturn(Mono.just(userEntity));
        when(mapper.map(userEntity, User.class)).thenReturn(user);
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mono<User> result = repositoryAdapter.saveUser(user);

        StepVerifier.create(result)
                .expectNextMatches(saved -> saved.getEmail().equals("test@email.com"))
                .verifyComplete();
    }
}
