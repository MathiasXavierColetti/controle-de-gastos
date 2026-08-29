package com.mathias.coletti.controledegastos.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioResetSenhaDTO {
    @NotBlank(message = "A nova senha não pode ser vazia")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    private String novaSenha;
}