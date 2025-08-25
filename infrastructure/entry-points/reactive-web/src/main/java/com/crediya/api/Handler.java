
package com.crediya.api;

import com.crediya.api.dto.CreateUserDTO;
import com.crediya.api.mapper.UserDTOMapper;
import com.crediya.library.api.BaseHandler;
import com.crediya.model.user.gateways.UserInputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.http.ResponseEntity.notFound;

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

    public Mono<ServerResponse> listenGetAllUsers(ServerRequest serverRequest) {
        log.debug("Recibiendo petición para obtener todos los usuarios");

        return userInputPort.getAllUsers()
                .map(userDTOMapper::toResponse)
                .collectList()
                .flatMap(userResponses -> ok("Usuarios obtenidos exitosamente", userResponses));
    }

    public Mono<ServerResponse> listenUpdateUser(ServerRequest serverRequest) {
        log.debug("Recibiendo petición para actualizar usuario");

        return serverRequest.bodyToMono(CreateUserDTO.class)
                .doOnNext(dto -> log.debug("Payload recibido: {}", dto))
                .map(userDTOMapper::toModel)
                .flatMap(userInputPort::updateUser)
                .map(userDTOMapper::toResponse)
                .flatMap(userResponse -> ok("Usuario actualizado exitosamente", userResponse));
    }

    public Mono<ServerResponse> listenFindByIdentityDocument(ServerRequest serverRequest) {
        String identityDocument = serverRequest.pathVariable("identityDocument");
        log.debug("Recibiendo petición para buscar usuario por documento de identidad: {}", identityDocument);

        return userInputPort.findByIdentityDocument(identityDocument)
                .map(userDTOMapper::toResponse)
                .flatMap(userResponse -> ok("Usuario encontrado exitosamente", userResponse));
    }
}
