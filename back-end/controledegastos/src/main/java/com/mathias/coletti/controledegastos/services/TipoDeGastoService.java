package com.mathias.coletti.controledegastos.services;

import com.mathias.coletti.controledegastos.dtos.TipoDeGastoCadastroDTO;
import com.mathias.coletti.controledegastos.dtos.TipoDeGastoResponseDTO;
import com.mathias.coletti.controledegastos.exceptions.BusinessException;
import com.mathias.coletti.controledegastos.exceptions.ResourceNotFoundException;
import com.mathias.coletti.controledegastos.models.TipoDeGasto;
import com.mathias.coletti.controledegastos.repositories.TipoDeGastoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoDeGastoService {

    private final TipoDeGastoRepository tipoDeGastoRepository;

    @Transactional
    public TipoDeGastoResponseDTO cadastrar(TipoDeGastoCadastroDTO dto) {
        if (tipoDeGastoRepository.existsByNomeIgnoreCase(dto.nome())) {
            throw new BusinessException("Já existe uma categoria cadastrada com este nome.");
        }

        TipoDeGasto tipo = new TipoDeGasto();
        tipo.setNome(dto.nome());
        tipo = tipoDeGastoRepository.save(tipo);

        return new TipoDeGastoResponseDTO(tipo.getId(), tipo.getNome());
    }

    @Transactional(readOnly = true)
    public List<TipoDeGastoResponseDTO> listarTodos() {
        return tipoDeGastoRepository.findAll().stream()
                .map(t -> new TipoDeGastoResponseDTO(t.getId(), t.getNome()))
                .toList();
    }

    @Transactional(readOnly = true)
    public TipoDeGastoResponseDTO buscarPorId(Long id) {
        TipoDeGasto tipo = tipoDeGastoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com o ID: " + id));

        return new TipoDeGastoResponseDTO(tipo.getId(), tipo.getNome());
    }
}