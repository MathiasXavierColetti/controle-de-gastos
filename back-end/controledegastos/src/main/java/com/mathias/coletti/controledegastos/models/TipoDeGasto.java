package com.mathias.coletti.controledegastos.models;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_tipo_de_gasto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TipoDeGasto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    private String descricao;
}
