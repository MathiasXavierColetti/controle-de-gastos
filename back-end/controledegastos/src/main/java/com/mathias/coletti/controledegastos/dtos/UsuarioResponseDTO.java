package com.mathias.coletti.controledegastos.dtos;

import com.mathias.coletti.controledegastos.models.Usuario;

public record UsuarioResponseDTO(Long id,
                                 String nome,
                                 String cpf

) {
    public UsuarioResponseDTO(Usuario usuario) {
        this(
                usuario.getId(),
                usuario.getPessoa() != null ? usuario.getPessoa().getNome() : null,
                usuario.getPessoa() != null ? usuario.getPessoa().getCpf() : null
        );
    }
}
