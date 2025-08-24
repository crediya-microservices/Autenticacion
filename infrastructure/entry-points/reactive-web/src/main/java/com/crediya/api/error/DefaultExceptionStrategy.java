package com.crediya.api.error;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class DefaultExceptionStrategy implements ExceptionStrategy {
    @Override
    public boolean supports(Throwable ex) {
        return true; // fallback
    }

    @Override
    public HttpStatus getStatus(Throwable ex) {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public String getError(Throwable ex) {
        return "Internal Server Error";
    }
}
