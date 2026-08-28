package com.mathias.coletti.controledegastos.controllers;

import com.mathias.coletti.controledegastos.dtos.AdicionarMembroDTO;
import com.mathias.coletti.controledegastos.dtos.GrupoCriacaoDTO;
import com.mathias.coletti.controledegastos.dtos.GrupoResponseDTO;
import com.mathias.coletti.controledegastos.models.Usuario;
import com.mathias.coletti.controledegastos.services.GrupoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/grupos")
@RequiredArgsConstructor
public class GrupoController {

    private final GrupoService grupoService;

    @PostMapping
    public ResponseEntity<GrupoResponseDTO> criarGrupo(@RequestBody @Valid GrupoCriacaoDTO dto) {
        GrupoResponseDTO response = grupoService.criarGrupo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/meus")
    public ResponseEntity<List<GrupoResponseDTO>> listarGruposDoUsuarioLogado() {
        List<GrupoResponseDTO> response = grupoService.listarGruposDoUsuarioLogado();
        return ResponseEntity.ok(response);
    }

    // Mapeamento específico para a rota /todos
    @GetMapping("/todos")
    public ResponseEntity<List<GrupoResponseDTO>> listarTodos() {
        List<GrupoResponseDTO> response = grupoService.listarTodos();
        return ResponseEntity.ok(response);
    }

    // O parâmetro id numérico deve ficar abaixo das rotas nomeadas
    @GetMapping("/{id}")
    public ResponseEntity<GrupoResponseDTO> buscarPorId(@PathVariable Long id) {
        Usuario usuarioLogado = grupoService.obterUsuarioLogado();
        GrupoResponseDTO response = grupoService.buscarPorId(usuarioLogado.getId(), id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GrupoResponseDTO> atualizarGrupo(
            @PathVariable Long id,
            @RequestBody @Valid GrupoCriacaoDTO dto
    ) {
        Usuario usuarioLogado = grupoService.obterUsuarioLogado();
        GrupoResponseDTO response = grupoService.atualizarGrupo(usuarioLogado.getId(), id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarGrupo(@PathVariable Long id) {
        Usuario usuarioLogado = grupoService.obterUsuarioLogado();
        grupoService.deletarGrupo(usuarioLogado.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/membros")
    public ResponseEntity<GrupoResponseDTO> adicionarMembro(
            @PathVariable Long id,
            @RequestBody @Valid AdicionarMembroDTO dto
    ) {
        Usuario usuarioLogado = grupoService.obterUsuarioLogado();
        GrupoResponseDTO response = grupoService.adicionarMembroPorCpf(usuarioLogado.getId(), id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{grupoId}/membros/{membroId}")
    public ResponseEntity<GrupoResponseDTO> removerMembro(
            @PathVariable Long grupoId,
            @PathVariable Long membroId
    ) {
        Usuario usuarioLogado = grupoService.obterUsuarioLogado();
        GrupoResponseDTO response = grupoService.removerMembro(usuarioLogado.getId(), grupoId, membroId);
        return ResponseEntity.ok(response);
    }
}