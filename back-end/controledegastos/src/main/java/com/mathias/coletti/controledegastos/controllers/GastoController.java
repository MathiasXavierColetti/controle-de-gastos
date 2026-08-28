package com.mathias.coletti.controledegastos.controllers;

import com.mathias.coletti.controledegastos.dtos.GastoCadastroDTO;
import com.mathias.coletti.controledegastos.dtos.GastoResponseDTO;
import com.mathias.coletti.controledegastos.repositories.projections.RelatorioCategoriaProjection;
import com.mathias.coletti.controledegastos.repositories.projections.RelatorioDetalhadoProjection;
import com.mathias.coletti.controledegastos.repositories.projections.RelatorioPessoaProjection;
import com.mathias.coletti.controledegastos.services.GastoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/gastos")
@RequiredArgsConstructor
public class GastoController {

    private final GastoService gastoService;

    @PostMapping
    public ResponseEntity<GastoResponseDTO> cadastrar(
            Authentication authentication,
            @RequestBody @Valid GastoCadastroDTO dto
    ) {
        Long usuarioAutenticadoId = (Long) authentication.getPrincipal();
        GastoResponseDTO response = gastoService.cadastrar(usuarioAutenticadoId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<GastoResponseDTO>> buscarPorGrupoEPeriodo(
            Authentication authentication,
            @PathVariable Long grupoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        Long usuarioAutenticadoId = (Long) authentication.getPrincipal();
        List<GastoResponseDTO> response = gastoService.buscarPorGrupoEPeriodo(usuarioAutenticadoId, grupoId, inicio, fim);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/grupo/{grupoId}/relatorios/categoria")
    public ResponseEntity<List<RelatorioCategoriaProjection>> obterRelatorioPorCategoria(
            Authentication authentication,
            @PathVariable Long grupoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        Long usuarioAutenticadoId = (Long) authentication.getPrincipal();
        List<RelatorioCategoriaProjection> response = gastoService.obterRelatorioPorCategoria(usuarioAutenticadoId, grupoId, inicio, fim);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/grupo/{grupoId}/relatorios/pessoa")
    public ResponseEntity<List<RelatorioPessoaProjection>> obterRelatorioPorPessoa(
            Authentication authentication,
            @PathVariable Long grupoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        Long usuarioAutenticadoId = (Long) authentication.getPrincipal();
        List<RelatorioPessoaProjection> response = gastoService.obterRelatorioPorPessoa(usuarioAutenticadoId, grupoId, inicio, fim);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/grupo/{grupoId}/relatorios/detalhado")
    public ResponseEntity<List<RelatorioDetalhadoProjection>> obterRelatorioPorPessoaECategoria(
            Authentication authentication,
            @PathVariable Long grupoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim
    ) {
        Long usuarioAutenticadoId = (Long) authentication.getPrincipal();
        List<RelatorioDetalhadoProjection> response = gastoService.obterRelatorioPorPessoaECategoria(usuarioAutenticadoId, grupoId, inicio, fim);
        return ResponseEntity.ok(response);
    }
}