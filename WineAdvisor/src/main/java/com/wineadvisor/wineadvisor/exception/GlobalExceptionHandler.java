package com.wineadvisor.wineadvisor.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import io.swagger.v3.oas.annotations.Hidden;


import jakarta.validation.ConstraintViolationException;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    //////// PARAMETRES PATTERN VIOLATION EXCEPTIONS ////////
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
        System.out.println("--- WRN: ConstraintViolationException thrown and intercepted.");

        String errorMessage = e.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("Validation error: some fields are invalid.");

        System.err.println("--- ERR: " + errorMessage + "\n");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("--- ERR: " + errorMessage);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        System.out.println("--- WRN: MethodArgumentNotValidException thrown and intercepted.");

        List<? extends MessageSourceResolvable> errors = e.getAllErrors();
        Integer errorCount = errors.size();
        
        String errorMessage = "";
        if (errorCount == 1) {
            errorMessage = errors.get(0).getDefaultMessage();
        } else {
            Integer i = 1;
            for (MessageSourceResolvable error : errors) {
                errorMessage += "\n--- Error n° " + i + ": " + error.getDefaultMessage();
                i++;
            }
        }
        
        System.err.println("--- ERR: " + errorMessage + "\n");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("--- ERR: " + errorMessage);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<?> handleHandlerMethodValidationException(HandlerMethodValidationException e) {
        System.out.println("--- WRN: HandlerMethodValidationException thrown and intercepted.");

        List<? extends MessageSourceResolvable> errors = e.getAllErrors();
        Integer errorCount = errors.size();
        
        String errorMessage = "";
        if (errorCount == 1) {
            errorMessage = errors.get(0).getDefaultMessage();
        } else {
            Integer i = 1;
            for (MessageSourceResolvable error : errors) {
                errorMessage += "\n--- Error n° " + i + ": " + error.getDefaultMessage();
                i++;
            }
        }
        
        System.err.println("--- ERR: " + errorMessage + "\n");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("--- ERR: " + errorMessage);
    }


    //////// ILLEGAL ARGUMENT EXCEPTION ////////
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        System.out.println("--- WRN: IllegalArgumentException thrown and intercepted.");
        System.err.println("--- ERR: " + e.getMessage() + "\n");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("--- ERR: " + e.getMessage());
    }


    //////// BAD REQUEST EXCEPTION (MY EXCEPTION) ////////
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException e) {
        System.out.println("--- WRN: BadRequestException thrown and intercepted.");
        System.err.println("--- ERR: " + e.getMessage() + "\n");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("--- ERR: " + e.getMessage());
    }


    //////// CONFLICT EXCEPTION (MY EXCEPTION) ////////
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<?> handleResourceAlreadyExistsException(ResourceAlreadyExistsException e) {
        System.out.println("--- WRN: ResourceAlreadyExistsException thrown and intercepted.");
        System.err.println("--- ERR: " + e.getMessage() + "\n");
        return ResponseEntity.status(HttpStatus.CONFLICT).body("--- ERR: " + e.getMessage());
    }


    //////// NOT FOUND EXCEPTION (MY EXCEPTION) ////////
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException e) {
        System.out.println("--- WRN: ResourceNotFoundException thrown and intercepted.");
        System.err.println("--- ERR: " + e.getMessage() + "\n");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("--- ERR: " + e.getMessage());
    }


    //////// OTHER EXCEPTIONS (GENERIC HANDLER) ////////
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        System.out.println("--- WRN: Generic exception thrown and intercepted.\n--- WRN: Exception type: " + e.getClass().getName());
        System.err.println("--- ERR: " + e.getMessage() + "\n");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("--- ERR: " + e.getMessage());
    }
}
