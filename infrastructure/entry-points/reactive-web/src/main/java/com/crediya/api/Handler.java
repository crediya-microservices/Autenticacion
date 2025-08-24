
package com.crediya.api;

import com.crediya.api.dto.CreateUserDTO;
import com.crediya.api.mapper.UserDTOMapper;
import com.crediya.model.user.gateways.UserInputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler extends BaseHandler {

    private final UserInputPort userInputPort;
    private final UserDTOMapper userDTOMapper;

    public Mono<ServerResponse> listenSaveUser(ServerRequest serverRequest) {
        log.debug("Recibiendo petición para crear usuario");

        return serverRequest.bodyToMono(CreateUserDTO.class)
                .doOnNext(dto -> log.debug("Payload recibido: {}", dto))
                .map(userDTOMapper::toModel)
                .flatMap(userInputPort::saveUser)
                .map(userDTOMapper::toResponse)
                .flatMap(userResponse -> created("Usuario creado exitosamente", userResponse));
    }
}
