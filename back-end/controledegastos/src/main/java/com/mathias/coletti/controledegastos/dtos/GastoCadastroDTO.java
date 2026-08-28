package com.mathias.coletti.controledegastos.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GastoCadastroDTO {

        @NotBlank(message = "A descrição do gasto é obrigatória.")
        private String descricao;

        @NotNull(message = "O valor do gasto é obrigatório.")
        @Positive(message = "O valor do gasto deve ser maior que zero.")
        private BigDecimal valor;

        @NotNull(message = "A data do gasto é obrigatória.")
        private LocalDate data;

        @NotNull(message = "O tipo de gasto é obrigatório.")
        private Long tipoDeGastoId;

        @NotNull(message = "O grupo é obrigatório.")
        private Long grupoId;
}