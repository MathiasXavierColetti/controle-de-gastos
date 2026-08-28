package com.mathias.coletti.controledegastos.dtos;

import jakarta.validation.constraints.NotBlank;

public record GrupoCriacaoDTO(
        @NotBlank(message = "O nome do grupo é obrigatório")
        String nome,

        String descricao
) {
}
