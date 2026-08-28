package com.mathias.coletti.controledegastos.repositories.projections;

import java.math.BigDecimal;

public interface RelatorioDetalhadoProjection {
    String getNomePessoa();
    String getCategoria();
    BigDecimal getTotal();
}
