package com.mathias.coletti.controledegastos.services;

import com.mathias.coletti.controledegastos.dtos.GastoCadastroDTO;
import com.mathias.coletti.controledegastos.dtos.GastoResponseDTO;
import com.mathias.coletti.controledegastos.models.Gasto;
import com.mathias.coletti.controledegastos.models.Grupo;
import com.mathias.coletti.controledegastos.models.TipoDeGasto;
import com.mathias.coletti.controledegastos.models.Usuario;
import com.mathias.coletti.controledegastos.repositories.GastoRepository;
import com.mathias.coletti.controledegastos.repositories.GrupoRepository;
import com.mathias.coletti.controledegastos.repositories.TipoDeGastoRepository;
import com.mathias.coletti.controledegastos.repositories.UsuarioRepository;
import com.mathias.coletti.controledegastos.repositories.projections.RelatorioCategoriaProjection;
import com.mathias.coletti.controledegastos.repositories.projections.RelatorioDetalhadoProjection;
import com.mathias.coletti.controledegastos.repositories.projections.RelatorioPessoaProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GastoService {

    private final GastoRepository gastoRepository;
    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TipoDeGastoRepository tipoDeGastoRepository;

    @Transactional
    public GastoResponseDTO cadastrar(Long usuarioAutenticadoId, GastoCadastroDTO dto) {
        // 1. Busca e valida se o grupo existe e se o usuário pertence a ele
        Grupo grupo = buscarEValidarGrupo(usuarioAutenticadoId, dto.grupoId());

        // 2. Busca o usuário pagador
        Usuario usuario = usuarioRepository.findById(usuarioAutenticadoId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        // 3. Busca a categoria (TipoDeGasto)
        TipoDeGasto tipoDeGasto = tipoDeGastoRepository.findById(dto.tipoDeGastoId())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de gasto não encontrado."));

        // 4. Monta e persiste o gasto
        Gasto gasto = Gasto.builder()
                .descricao(dto.descricao())
                .valor(dto.valor())
                .data(dto.data())
                .grupo(grupo)
                .usuario(usuario)
                .tipoDeGasto(tipoDeGasto)
                .build();

        Gasto salvo = gastoRepository.save(gasto);

        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<GastoResponseDTO> buscarPorGrupoEPeriodo(
            Long usuarioAutenticadoId,
            Long grupoId,
            LocalDate inicio,
            LocalDate fim
    ) {
        buscarEValidarGrupo(usuarioAutenticadoId, grupoId);

        return gastoRepository.findByGrupoIdAndDataBetweenOrderByDataDesc(grupoId, inicio, fim)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    // --- RELATÓRIOS ---

    @Transactional(readOnly = true)
    public List<RelatorioCategoriaProjection> obterRelatorioPorCategoria(
            Long usuarioAutenticadoId,
            Long grupoId,
            LocalDate inicio,
            LocalDate fim
    ) {
        buscarEValidarGrupo(usuarioAutenticadoId, grupoId);
        return gastoRepository.relatorioPorCategoria(grupoId, inicio, fim);
    }

    @Transactional(readOnly = true)
    public List<RelatorioPessoaProjection> obterRelatorioPorPessoa(
            Long usuarioAutenticadoId,
            Long grupoId,
            LocalDate inicio,
            LocalDate fim
    ) {
        buscarEValidarGrupo(usuarioAutenticadoId, grupoId);
        return gastoRepository.relatorioPorPessoa(grupoId, inicio, fim);
    }

    @Transactional(readOnly = true)
    public List<RelatorioDetalhadoProjection> obterRelatorioPorPessoaECategoria(
            Long usuarioAutenticadoId,
            Long grupoId,
            LocalDate inicio,
            LocalDate fim
    ) {
        buscarEValidarGrupo(usuarioAutenticadoId, grupoId);
        return gastoRepository.relatorioPorPessoaECategoria(grupoId, inicio, fim);
    }

    // --- MÉTODOS AUXILIARES ---

    private Grupo buscarEValidarGrupo(Long usuarioId, Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new IllegalArgumentException("Grupo não encontrado."));

        boolean pertence = grupo.getUsuarios().stream()
                .anyMatch(u -> u.getId().equals(usuarioId));

        if (!pertence) {
            throw new AccessDeniedException("Acesso negado: Você não faz parte deste grupo.");
        }

        return grupo;
    }

    private GastoResponseDTO converterParaDTO(Gasto gasto) {
        return new GastoResponseDTO(
                gasto.getId(),
                gasto.getDescricao(),
                gasto.getValor(),
                gasto.getData(),
                gasto.getTipoDeGasto().getId(),
                gasto.getTipoDeGasto().getNome(),
                gasto.getUsuario().getId(),
                gasto.getUsuario().getPessoa().getNome(),
                gasto.getGrupo().getId(),
                gasto.getGrupo().getNome()
        );
    }
}