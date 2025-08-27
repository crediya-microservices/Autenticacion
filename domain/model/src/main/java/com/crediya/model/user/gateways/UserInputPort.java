package com.crediya.model.user.gateways;

import com.crediya.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserInputPort {
    Mono<User> saveUser(User user);
    Mono<User> findByEmail (String email);
}
