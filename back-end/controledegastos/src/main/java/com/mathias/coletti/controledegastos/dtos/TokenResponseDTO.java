package com.mathias.coletti.controledegastos.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponseDTO {
    private String token;
    private String tipo;
    private Long usuarioId;
    private String nomePessoa;
}