package com.mathias.coletti.controledegastos.controllers;

import com.mathias.coletti.controledegastos.dtos.AdicionarMembroDTO;
import com.mathias.coletti.controledegastos.dtos.GrupoCriacaoDTO;
import com.mathias.coletti.controledegastos.dtos.GrupoResponseDTO;
import com.mathias.coletti.controledegastos.services.GrupoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/grupos")
@RequiredArgsConstructor
public class GrupoController {

    private final GrupoService grupoService;

    @PostMapping
    public ResponseEntity<GrupoResponseDTO> criarGrupo(
            Authentication authentication,
            @RequestBody @Valid GrupoCriacaoDTO dto
    ) {
        Long usuarioAutenticadoId = (Long) authentication.getPrincipal();
        GrupoResponseDTO response = grupoService.criarGrupo(usuarioAutenticadoId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{grupoId}/membros")
    public ResponseEntity<GrupoResponseDTO> adicionarMembro(
            Authentication authentication,
            @PathVariable Long grupoId,
            @RequestBody @Valid AdicionarMembroDTO dto
    ) {
        Long usuarioAutenticadoId = (Long) authentication.getPrincipal();
        GrupoResponseDTO response = grupoService.adicionarMembroPorCpf(usuarioAutenticadoId, grupoId, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<GrupoResponseDTO>> listarGruposDoUsuario(Authentication authentication) {
        Long usuarioAutenticadoId = (Long) authentication.getPrincipal();
        List<GrupoResponseDTO> response = grupoService.listarGruposDoUsuario(usuarioAutenticadoId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{grupoId}")
    public ResponseEntity<GrupoResponseDTO> buscarPorId(
            Authentication authentication,
            @PathVariable Long grupoId
    ) {
        Long usuarioAutenticadoId = (Long) authentication.getPrincipal();
        GrupoResponseDTO response = grupoService.buscarPorId(usuarioAutenticadoId, grupoId);
        return ResponseEntity.ok(response);
    }
}