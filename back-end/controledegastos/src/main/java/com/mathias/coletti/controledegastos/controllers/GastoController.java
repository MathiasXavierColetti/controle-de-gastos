package com.mathias.coletti.controledegastos.controllers;

import com.mathias.coletti.controledegastos.dtos.GastoRequestDTO;
import com.mathias.coletti.controledegastos.dtos.GastoResponseDTO;
import com.mathias.coletti.controledegastos.services.GastoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/gastos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GastoController {

    private final GastoService gastoService;

    @GetMapping("/relatorio")
    public ResponseEntity<List<GastoResponseDTO>> gerarRelatorio(
            @RequestParam Long grupoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) Long tipoDeGastoId) {

        List<GastoResponseDTO> relatorio = gastoService.buscarRelatorio(grupoId, inicio, fim, tipoDeGastoId);
        return ResponseEntity.ok(relatorio);
    }

    @PostMapping
    public ResponseEntity<GastoResponseDTO> criar(@Valid @RequestBody GastoRequestDTO dto) {
        GastoResponseDTO novoGasto = gastoService.criar(dto);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(novoGasto.getId())
                .toUri();

        return ResponseEntity.created(location).body(novoGasto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GastoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody GastoRequestDTO dto) {

        GastoResponseDTO gastoAtualizado = gastoService.atualizar(id, dto);
        return ResponseEntity.ok(gastoAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        gastoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}