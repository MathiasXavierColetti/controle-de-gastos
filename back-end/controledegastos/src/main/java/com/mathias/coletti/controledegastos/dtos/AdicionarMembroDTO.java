package com.mathias.coletti.controledegastos.dtos;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

public record AdicionarMembroDTO(@NotBlank(message = "O CPF é obrigatório")
                                 @CPF(message = "CPF inválido")
                                 String cpf) {

}
