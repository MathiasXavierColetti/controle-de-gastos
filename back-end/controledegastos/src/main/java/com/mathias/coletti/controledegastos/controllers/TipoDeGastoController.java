package com.mathias.coletti.controledegastos.controllers;

import com.mathias.coletti.controledegastos.dtos.TipoDeGastoCadastroDTO;
import com.mathias.coletti.controledegastos.dtos.TipoDeGastoResponseDTO;
import com.mathias.coletti.controledegastos.services.TipoDeGastoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/tipo-de-gasto", "/api/v1/tipos-de-gasto"})
@RequiredArgsConstructor
public class TipoDeGastoController {

    private final TipoDeGastoService tipoDeGastoService;

    @PostMapping
    public ResponseEntity<TipoDeGastoResponseDTO> cadastrar(@RequestBody @Valid TipoDeGastoCadastroDTO dto) {
        TipoDeGastoResponseDTO response = tipoDeGastoService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TipoDeGastoResponseDTO>> listarTodos() {
        List<TipoDeGastoResponseDTO> response = tipoDeGastoService.listarTodos();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoDeGastoResponseDTO> buscarPorId(@PathVariable Long id) {
        TipoDeGastoResponseDTO response = tipoDeGastoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoDeGastoResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid TipoDeGastoCadastroDTO dto
    ) {
        TipoDeGastoResponseDTO response = tipoDeGastoService.atualizar(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        tipoDeGastoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}