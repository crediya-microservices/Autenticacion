package com.crediya.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
public abstract class BaseHandler {

   protected <T> Mono<ServerResponse> ok(String message, T body) {
       log.debug("Respuesta OK: {} - {}", message, body);
       ApiResponse<T> response = new ApiResponse<>(message, body);
       return ServerResponse.ok().bodyValue(response);
   }

    protected <T> Mono<ServerResponse> ok(String message) {
        ApiResponse<T> response = new ApiResponse<>(message);
        return ServerResponse.ok().bodyValue(response);
    }

    protected <T> Mono<ServerResponse> created(String message,T body) {
        log.debug("Recurso creado: {}", body);
        ApiResponse<T> response = new ApiResponse<>(message, body);
        return ServerResponse.status(HttpStatus.CREATED).bodyValue(response);
    }

}
