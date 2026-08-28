package com.mathias.coletti.controledegastos.repositories;

import com.mathias.coletti.controledegastos.models.Gasto;
import com.mathias.coletti.controledegastos.repositories.projections.RelatorioCategoriaProjection;
import com.mathias.coletti.controledegastos.repositories.projections.RelatorioDetalhadoProjection;
import com.mathias.coletti.controledegastos.repositories.projections.RelatorioPessoaProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface GastoRepository extends JpaRepository<Gasto, Long> {

    // 1. Relatório por Categoria no Período
    @Query("SELECT g.tipoDeGasto.nome AS categoria, SUM(g.valor) AS total " +
            "FROM Gasto g " +
            "WHERE g.grupo.id = :grupoId AND g.data BETWEEN :inicio AND :fim " +
            "GROUP BY g.tipoDeGasto.nome " +
            "ORDER BY total DESC")
    List<RelatorioCategoriaProjection> relatorioPorCategoria(
            @Param("grupoId") Long grupoId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );
    List<Gasto> findByGrupoIdAndDataBetweenOrderByDataDesc(Long grupoId, LocalDate inicio, LocalDate fim);

    // 2. Relatório por Pessoa no Período
    @Query("SELECT g.usuario.pessoa.nome AS nomePessoa, SUM(g.valor) AS total " +
            "FROM Gasto g " +
            "WHERE g.grupo.id = :grupoId AND g.data BETWEEN :inicio AND :fim " +
            "GROUP BY g.usuario.pessoa.nome " +
            "ORDER BY total DESC")
    List<RelatorioPessoaProjection> relatorioPorPessoa(
            @Param("grupoId") Long grupoId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    // 3. Relatório Cruzado: Quanto cada pessoa gastou em cada categoria no período
    @Query("SELECT g.usuario.pessoa.nome AS nomePessoa, g.tipoDeGasto.nome AS categoria, SUM(g.valor) AS total " +
            "FROM Gasto g " +
            "WHERE g.grupo.id = :grupoId AND g.data BETWEEN :inicio AND :fim " +
            "GROUP BY g.usuario.pessoa.nome, g.tipoDeGasto.nome " +
            "ORDER BY g.usuario.pessoa.nome ASC, total DESC")
    List<RelatorioDetalhadoProjection> relatorioPorPessoaECategoria(
            @Param("grupoId") Long grupoId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );
}
