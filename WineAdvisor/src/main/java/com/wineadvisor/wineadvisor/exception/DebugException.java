package com.wineadvisor.wineadvisor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DebugException extends RuntimeException {
    public DebugException() {
        super("Invoked DebugException for debugging purposes.");
    }
}