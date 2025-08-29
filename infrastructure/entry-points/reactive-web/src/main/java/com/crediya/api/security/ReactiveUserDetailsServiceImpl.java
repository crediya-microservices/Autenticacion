package com.crediya.api.security;

import com.crediya.model.user.gateways.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    public ReactiveUserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String email) {
        return userRepository.getUserByEmail(email)
                .map(dbUser -> {
                    var authorities = java.util.List.of(new SimpleGrantedAuthority(dbUser.getRoleName()));
                    return User.withUsername(dbUser.getEmail())
                            .password(dbUser.getPassword())
                            .authorities(authorities)
                            .build();
                });
    }
}
