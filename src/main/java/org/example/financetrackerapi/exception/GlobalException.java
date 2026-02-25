package org.example.financetrackerapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalException {

    record ApiError(Instant timestamp, int status, String error, String message,String path ){}


    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgumentException(IllegalArgumentException ex, jakarta.servlet.http.HttpServletRequest request) {
        return new ApiError(Instant.now(), HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handlesAccessDenied(AccessDeniedException ex, jakarta.servlet.http.HttpServletRequest req){
        return new ApiError(Instant.now(),HttpStatus.FORBIDDEN.value(),"Forbidden",ex.getMessage(),req.getRequestURI());
    }

    @ExceptionHandler(BadCredentialException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handlesBadCredential(BadCredentialException ex, jakarta.servlet.http.HttpServletRequest req){
        return new ApiError(Instant.now(),HttpStatus.FORBIDDEN.value(), "Forbidden",ex.getMessage(),req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiError handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, jakarta.servlet.http.HttpServletRequest req){
        String msg = ex.getBindingResult().getAllErrors().stream()
                .map(e-> {
                    if (e instanceof FieldError fieldError){
                        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                    }
                    return e.getObjectName() + ": " + e.getDefaultMessage();
                })
                .collect(Collectors.joining(";"));

        if(msg.isBlank()){
            msg = "Validation failed";
        }

        return new ApiError(Instant.now(), HttpStatus.UNPROCESSABLE_ENTITY.value(), "Unprocessable Entity", msg, req.getRequestURI());
    }

    @ExceptionHandler(EmailAlreadyExistException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleEmailAlreadyExistException(EmailAlreadyExistException ex, jakarta.servlet.http.HttpServletRequest req){
        return new ApiError(Instant.now(),HttpStatus.CONFLICT.value(), "Email Already Exist", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(Exception ex, jakarta.servlet.http.HttpServletRequest request) {
        return new ApiError(Instant.now(), 500, "Internal Server Error", ex.getMessage(),request.getRequestURI());
    }


}
