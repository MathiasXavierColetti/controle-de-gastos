package com.mathias.coletti.controledegastos.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GastoResponseDTO(
        Long id,
        String descricao,
        BigDecimal valor,
        LocalDate data,
        Long tipoDeGastoId,
        String tipoDeGastoNome,
        Long usuarioId,
        String usuarioNome,
        Long grupoId,
        String grupoNome
) {}