package com.mathias.coletti.controledegastos.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandardErrorDTO {
    private Instant timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;
    private List<FieldErrorDTO> errors; // Preenchido apenas em erros de validação de campos

    public StandardErrorDTO(Instant timestamp, Integer status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    @Data
    @AllArgsConstructor
    public static class FieldErrorDTO {
        private String field;
        private String defaultMessage;
    }
}