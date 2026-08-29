package com.mathias.coletti.controledegastos.repositories;

import com.mathias.coletti.controledegastos.dtos.RelatorioGastoUsuarioDTO;
import com.mathias.coletti.controledegastos.models.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GastoRepository extends JpaRepository<Gasto, Long>, JpaSpecificationExecutor<Gasto> {

    List<Gasto> findByGrupoId(Long grupoId);

    @Query("SELECT g FROM Gasto g WHERE g.grupo.id = :grupoId AND g.data BETWEEN :inicio AND :fim")
    List<Gasto> findByGrupoAndPeriodo(
            @Param("grupoId") Long grupoId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query("SELECT g FROM Gasto g WHERE g.grupo.id = :grupoId AND g.tipoDeGasto.id = :tipoDeGastoId AND g.data BETWEEN :inicio AND :fim")
    List<Gasto> findByGrupoAndTipoAndPeriodo(
            @Param("grupoId") Long grupoId,
            @Param("tipoDeGastoId") Long tipoDeGastoId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );

    @Query("SELECT g FROM Gasto g WHERE " +
            "(:grupoId IS NULL OR g.grupo.id = :grupoId) " +
            "AND (cast(:inicio as java.time.LocalDate) IS NULL OR g.data >= :inicio) " +
            "AND (cast(:fim as java.time.LocalDate) IS NULL OR g.data <= :fim) " +
            "AND (:tipoDeGastoId IS NULL OR g.tipoDeGasto.id = :tipoDeGastoId)")
    List<Gasto> filtrarGastos(
            @Param("grupoId") Long grupoId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("tipoDeGastoId") Long tipoDeGastoId
    );
    @Query("SELECT new com.mathias.coletti.controledegastos.dtos.RelatorioGastoUsuarioDTO(" +
            "u.pessoa.nome, SUM(g.valor)) " +
            "FROM Gasto g JOIN g.usuario u " +
            "WHERE g.grupo.id = :grupoId " +
            "AND (cast(:inicio as java.time.LocalDate) IS NULL OR g.data >= :inicio) " +
            "AND (cast(:fim as java.time.LocalDate) IS NULL OR g.data <= :fim) " +
            "AND (:tipoDeGastoId IS NULL OR g.tipoDeGasto.id = :tipoDeGastoId) " +
            "GROUP BY u.pessoa.nome " +
            "ORDER BY SUM(g.valor) DESC")
    List<RelatorioGastoUsuarioDTO> relatorioPorUsuarioEGrupo(
            @Param("grupoId") Long grupoId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("tipoDeGastoId") Long tipoDeGastoId
    );
}