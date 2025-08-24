package com.crediya.api.error;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GlobalErrorHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        String message;

        if (ex instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
            message = ex.getMessage();
        } else if (ex instanceof IllegalStateException) {
            status = HttpStatus.CONFLICT;
            message = ex.getMessage();
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "Error inesperado: " + ex.getMessage();
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("{\"error\":\"%s\"}", message);

        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(body.getBytes())));
    }
}
