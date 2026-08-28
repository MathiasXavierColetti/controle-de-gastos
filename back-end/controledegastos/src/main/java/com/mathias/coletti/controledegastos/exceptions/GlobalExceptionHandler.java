package com.mathias.coletti.controledegastos.exceptions;

import com.mathias.coletti.controledegastos.dtos.StandardErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Trata erros de recursos não encontrados (404 Not Found)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardErrorDTO> resourceNotFound(ResourceNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        StandardErrorDTO err = new StandardErrorDTO(
                Instant.now(),
                status.value(),
                "Recurso não encontrado",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }

    // 2. Trata erros de regras de negócio ou duplicidades (400 Bad Request)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<StandardErrorDTO> businessError(BusinessException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardErrorDTO err = new StandardErrorDTO(
                Instant.now(),
                status.value(),
                "Regra de negócio violada",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }

    // 3. Trata acesso negado a grupos/recursos (403 Forbidden)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardErrorDTO> accessDenied(AccessDeniedException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        StandardErrorDTO err = new StandardErrorDTO(
                Instant.now(),
                status.value(),
                "Acesso negado",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }

    // 4. Trata erros de validação do @Valid (400 Bad Request)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardErrorDTO> validation(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        List<StandardErrorDTO.FieldErrorDTO> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new StandardErrorDTO.FieldErrorDTO(f.getField(), f.getDefaultMessage()))
                .toList();

        StandardErrorDTO err = new StandardErrorDTO(
                Instant.now(),
                status.value(),
                "Erro de validação de dados",
                "Um ou mais campos estão inválidos",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.status(status).body(err);
    }

    // 5. Trata qualquer outra exceção não esperada (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardErrorDTO> genericError(Exception e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        StandardErrorDTO err = new StandardErrorDTO(
                Instant.now(),
                status.value(),
                "Erro interno no servidor",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(err);
    }
}