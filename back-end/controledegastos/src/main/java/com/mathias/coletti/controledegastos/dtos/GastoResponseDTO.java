package com.mathias.coletti.controledegastos.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GastoResponseDTO {
    private Long id;
    private String descricao;
    private BigDecimal valor;
    private LocalDate data;
    private Long grupoId;
    private String grupoNome;
    private Long tipoDeGastoId;
    private String tipoDeGastoNome;
    private String usuarioNome;

    public GastoResponseDTO(Long id, String descricao, BigDecimal valor, LocalDate data,
                            Long grupoId, String grupoNome, Long tipoDeGastoId,
                            String tipoDeGastoNome, String usuarioNome) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.grupoId = grupoId;
        this.grupoNome = grupoNome;
        this.tipoDeGastoId = tipoDeGastoId;
        this.tipoDeGastoNome = tipoDeGastoNome;
        this.usuarioNome = usuarioNome;
    }

    // Getters
    public Long getId() { return id; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValor() { return valor; }
    public LocalDate getData() { return data; }
    public Long getGrupoId() { return grupoId; }
    public String getGrupoNome() { return grupoNome; }
    public Long getTipoDeGastoId() { return tipoDeGastoId; }
    public String getTipoDeGastoNome() { return tipoDeGastoNome; }
    public String getUsuarioNome() { return usuarioNome; }
}