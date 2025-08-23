package com.crediya.api;

import com.crediya.api.dto.CreateUserDTO;
import com.crediya.api.dto.UserDTO;
import com.crediya.api.mapper.UserDTOMapper;
import com.crediya.usecase.user.UserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class Handler {

    private final UserUseCase userUseCase;
    private final UserDTOMapper userDTOMapper;

    public Mono<ServerResponse> listenSaveUser(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateUserDTO.class)
                .map(userDTOMapper::toModel)
                .flatMap(userUseCase::saveUser)
                .map(userDTOMapper::toResponse)
                .flatMap(userDTO -> ServerResponse.ok().bodyValue(userDTO))
                .switchIfEmpty(ServerResponse.badRequest().build());
    }

    public Mono<ServerResponse> listenGETOtherUseCase(ServerRequest serverRequest) {
        // useCase2.logic();
        return ServerResponse.ok().bodyValue("");
    }

    public Mono<ServerResponse> listenPOSTUseCase(ServerRequest serverRequest) {
        // useCase.logic();
        return ServerResponse.ok().bodyValue("");
    }
}
