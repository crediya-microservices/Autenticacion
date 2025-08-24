package com.crediya.api.error;

import org.springframework.http.HttpStatus;

public interface ExceptionStrategy {
    boolean supports(Throwable ex);
    HttpStatus getStatus(Throwable ex);
    String getError(Throwable ex);
}
