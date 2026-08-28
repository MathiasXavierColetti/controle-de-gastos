package com.mathias.coletti.controledegastos.dtos;

import java.util.List;

public record GrupoResponseDTO (Long id,
                                String nome,
                                String descricao,
                                List<UsuarioResponseDTO> membros){
}
