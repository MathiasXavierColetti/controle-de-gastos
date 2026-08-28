package com.mathias.coletti.controledegastos.dtos;

public record UsuarioAtualizacaoDTO(
        String nome,
        String usuario,
        String senhaAtual,
        String novaSenha
) {
}
