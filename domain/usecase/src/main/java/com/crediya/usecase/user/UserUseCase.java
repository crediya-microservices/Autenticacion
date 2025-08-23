package com.crediya.usecase.user;


import com.crediya.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserUseCase {

    private final UserRepository userRepository;


}
