package com.mathias.coletti.controledegastos.dtos;

import java.math.BigDecimal;

public record RelatorioGastoUsuarioDTO(
        String usuario,
        BigDecimal valorTotal,
        Double porcentagem
) {
    public RelatorioGastoUsuarioDTO(String usuario, BigDecimal valorTotal) {
        this(usuario, valorTotal, 0.0);
    }
}