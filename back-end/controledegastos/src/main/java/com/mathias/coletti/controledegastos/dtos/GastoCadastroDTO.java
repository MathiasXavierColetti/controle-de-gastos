package com.mathias.coletti.controledegastos.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GastoCadastroDTO(
        @NotBlank(message = "A descrição do gasto é obrigatória")
        String descricao,

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor do gasto deve ser maior que zero")
        BigDecimal valor,

        @NotNull(message = "A data do gasto é obrigatória")
        LocalDate data,

        @NotNull(message = "O ID do tipo de gasto é obrigatório")
        Long tipoDeGastoId,

        @NotNull(message = "O ID do grupo é obrigatório")
        Long grupoId
) {}