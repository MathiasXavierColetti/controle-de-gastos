package com.mathias.coletti.controledegastos.dtos;

import com.mathias.coletti.controledegastos.models.Grupo;

import java.util.List;
import java.util.stream.Collectors;

public record GrupoResponseDTO (Long id,
                                String nome,
                                String descricao,
                                List<UsuarioResponseDTO> membros){
    // Construtor que mapeia a entidade Grupo para o DTO automaticamente
    public GrupoResponseDTO(Grupo grupo) {
        this(
                grupo.getId(),
                grupo.getNome(),
                grupo.getDescricao(),
                grupo.getUsuarios() != null
                        ? grupo.getUsuarios().stream()
                        .map(UsuarioResponseDTO::new) // Certifique-se de que UsuarioResponseDTO possui um construtor que aceita (Usuario usuario)
                        .collect(Collectors.toList())
                        : List.of()
        );
    }
}
