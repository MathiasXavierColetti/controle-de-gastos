package com.mathias.coletti.controledegastos.dtos;

import jakarta.validation.constraints.NotBlank;

public record TipoDeGastoCadastroDTO(
        @NotBlank(message = "O nome do tipo de gasto é obrigatório")
        String nome,

        String descricao
) {
}
