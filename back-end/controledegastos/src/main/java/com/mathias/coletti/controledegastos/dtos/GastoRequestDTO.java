package com.mathias.coletti.controledegastos.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class GastoRequestDTO {

    @NotBlank(message = "A descrição é obrigatória.")
    private String descricao;

    @NotNull(message = "O valor é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
    private BigDecimal valor;

    private LocalDate data;

    @NotNull(message = "O ID do grupo é obrigatório.")
    private Long grupoId;

    @NotNull(message = "O ID do tipo de gasto é obrigatório.")
    private Long tipoDeGastoId;

    // Getters e Setters
    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public Long getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(Long grupoId) {
        this.grupoId = grupoId;
    }

    public Long getTipoDeGastoId() {
        return tipoDeGastoId;
    }

    public void setTipoDeGastoId(Long tipoDeGastoId) {
        this.tipoDeGastoId = tipoDeGastoId;
    }
}