package com.mathias.coletti.controledegastos.services;

import com.mathias.coletti.controledegastos.dtos.GastoRequestDTO;
import com.mathias.coletti.controledegastos.dtos.GastoResponseDTO;
import com.mathias.coletti.controledegastos.exceptions.ResourceNotFoundException;
import com.mathias.coletti.controledegastos.models.Gasto;
import com.mathias.coletti.controledegastos.models.Grupo;
import com.mathias.coletti.controledegastos.models.TipoDeGasto;
import com.mathias.coletti.controledegastos.models.Usuario;
import com.mathias.coletti.controledegastos.repositories.GastoRepository;
import com.mathias.coletti.controledegastos.repositories.GrupoRepository;
import com.mathias.coletti.controledegastos.repositories.TipoDeGastoRepository;
import com.mathias.coletti.controledegastos.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GastoService {

    private final GastoRepository gastoRepository;
    private final UsuarioRepository usuarioRepository;
    private final GrupoRepository grupoRepository;
    private final TipoDeGastoRepository tipoDeGastoRepository;

    private Usuario getUsuarioLogado() {
        // Puxa o CPF que foi gravado na sessão do Spring Security pelo filtro
        String cpfSessao = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String cpfLimpo = (cpfSessao != null) ? cpfSessao.replaceAll("\\D", "") : "";

        return usuarioRepository.findByPessoaCpf(cpfLimpo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado para o CPF: " + cpfLimpo));
    }

    @Transactional(readOnly = true)
    public List<GastoResponseDTO> buscarRelatorio(Long grupoId, LocalDate inicio, LocalDate fim, Long tipoDeGastoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado com ID: " + grupoId));

        Usuario usuarioLogado = getUsuarioLogado();
        validarPertencimentoAoGrupo(usuarioLogado.getId(), grupo);

        List<Gasto> gastos = gastoRepository.filtrarGastos(grupoId, inicio, fim, tipoDeGastoId);

        return gastos.stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional
    public GastoResponseDTO criar(GastoRequestDTO dto) {
        Usuario usuarioLogado = getUsuarioLogado();

        Grupo grupo = grupoRepository.findById(dto.getGrupoId())
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado."));

        validarPertencimentoAoGrupo(usuarioLogado.getId(), grupo);

        TipoDeGasto tipoDeGasto = tipoDeGastoRepository.findById(dto.getTipoDeGastoId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de Gasto não encontrado."));

        Gasto gasto = Gasto.builder()
                .descricao(dto.getDescricao())
                .valor(dto.getValor())
                .data(dto.getData() != null ? dto.getData() : LocalDate.now())
                .grupo(grupo)
                .tipoDeGasto(tipoDeGasto)
                .usuario(usuarioLogado)
                .build();

        Gasto salvo = gastoRepository.save(gasto);
        return converterParaDTO(salvo);
    }

    @Transactional
    public GastoResponseDTO atualizar(Long id, GastoRequestDTO dto) {
        Usuario usuarioLogado = getUsuarioLogado();

        Gasto gasto = gastoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto não encontrado com ID: " + id));

        validarPertencimentoAoGrupo(usuarioLogado.getId(), gasto.getGrupo());

        Grupo grupo = grupoRepository.findById(dto.getGrupoId())
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado."));

        validarPertencimentoAoGrupo(usuarioLogado.getId(), grupo);

        TipoDeGasto tipoDeGasto = tipoDeGastoRepository.findById(dto.getTipoDeGastoId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de Gasto não encontrado."));

        gasto.setDescricao(dto.getDescricao());
        gasto.setValor(dto.getValor());
        gasto.setData(dto.getData() != null ? dto.getData() : gasto.getData());
        gasto.setGrupo(grupo);
        gasto.setTipoDeGasto(tipoDeGasto);

        Gasto atualizado = gastoRepository.save(gasto);
        return converterParaDTO(atualizado);
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuarioLogado = getUsuarioLogado();

        Gasto gasto = gastoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto não encontrado para exclusão."));

        validarPertencimentoAoGrupo(usuarioLogado.getId(), gasto.getGrupo());

        gastoRepository.delete(gasto);
    }

    private void validarPertencimentoAoGrupo(Long usuarioId, Grupo grupo) {
        boolean pertence = grupo.getUsuarios().stream()
                .anyMatch(u -> u.getId().equals(usuarioId));

        if (!pertence) {
            throw new AccessDeniedException("Usuário não possui acesso ao grupo informado.");
        }
    }

    private GastoResponseDTO converterParaDTO(Gasto gasto) {
        String nomeUsuario = (gasto.getUsuario() != null && gasto.getUsuario().getPessoa() != null)
                ? gasto.getUsuario().getPessoa().getNome()
                : "Não informado";

        return new GastoResponseDTO(
                gasto.getId(),
                gasto.getDescricao(),
                gasto.getValor(),
                gasto.getData(),
                gasto.getGrupo() != null ? gasto.getGrupo().getId() : null,
                gasto.getGrupo() != null ? gasto.getGrupo().getNome() : null,
                gasto.getTipoDeGasto() != null ? gasto.getTipoDeGasto().getId() : null,
                gasto.getTipoDeGasto() != null ? gasto.getTipoDeGasto().getNome() : "Outros",
                nomeUsuario
        );
    }


}