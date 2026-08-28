package com.mathias.coletti.controledegastos.repositories.projections;

import java.math.BigDecimal;

public interface RelatorioCategoriaProjection {
    String getCategoria();
    BigDecimal getTotal();
}
