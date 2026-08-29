package com.mathias.coletti.controledegastos.services;

import com.mathias.coletti.controledegastos.dtos.GastoRequestDTO;
import com.mathias.coletti.controledegastos.dtos.GastoResponseDTO;
import com.mathias.coletti.controledegastos.dtos.RelatorioGastoUsuarioDTO;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GastoService {

    private final GastoRepository gastoRepository;
    private final UsuarioRepository usuarioRepository;
    private final GrupoRepository grupoRepository;
    private final TipoDeGastoRepository tipoDeGastoRepository;

    private Usuario getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        Object principal = authentication.getPrincipal();
        String identificadorSessao;

        // 1. Extração da informação crua da sessão
        if (principal instanceof UserDetails userDetails) {
            identificadorSessao = userDetails.getUsername();
        } else if (principal instanceof String strPrincipal) {
            identificadorSessao = strPrincipal;
        } else {
            identificadorSessao = authentication.getName();
        }

        if (identificadorSessao == null || identificadorSessao.isBlank()) {
            throw new AccessDeniedException("Não foi possível identificar o usuário na sessão.");
        }

        String cpfLimpo = identificadorSessao.replaceAll("\\D", "");

        // 2. Primeira Consulta: Tenta buscar diretamente pelo CPF
        if (!cpfLimpo.isEmpty()) {
            return usuarioRepository.findByPessoaCpf(cpfLimpo)
                    .orElseGet(() -> buscarUsuarioPorOutroIdentificadorESegundaConsultaCpf(identificadorSessao));
        }

        // 3. Segunda Consulta (Fallback): Se a sessão continha outro identificador (ID ou Email)
        return buscarUsuarioPorOutroIdentificadorESegundaConsultaCpf(identificadorSessao);
    }

    private Usuario buscarUsuarioPorOutroIdentificadorESegundaConsultaCpf(String identificadorSessao) {
        // Consulta 1: Busca o usuário pelo ID ou outro parâmetro da sessão
        Usuario usuarioTemp;
        try {
            Long usuarioId = Long.parseLong(identificadorSessao);
            usuarioTemp = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado para o ID: " + usuarioId));
        } catch (NumberFormatException e) {
            throw new ResourceNotFoundException("Usuário não encontrado para o identificador da sessão: " + identificadorSessao);
        }

        // Garante a extração do CPF da pessoa vinculada
        if (usuarioTemp.getPessoa() == null || usuarioTemp.getPessoa().getCpf() == null) {
            throw new ResourceNotFoundException("Usuário encontrado, mas não possui CPF vinculado.");
        }

        String cpfExtraiDoUsuario = usuarioTemp.getPessoa().getCpf().replaceAll("\\D", "");

        // Consulta 2 (Forçada por ordem): Consulta novamente o repositório utilizando EXCLUSIVAMENTE o CPF extraído
        return usuarioRepository.findByPessoaCpf(cpfExtraiDoUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado na segunda consulta pelo CPF: " + cpfExtraiDoUsuario));
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

    @Transactional(readOnly = true)
    public List<RelatorioGastoUsuarioDTO> obterRelatorioPizza(Long grupoId, LocalDate inicio, LocalDate fim, Long tipoDeGastoId) {
        if (grupoId == null) {
            throw new IllegalArgumentException("O ID do grupo é obrigatório para gerar o relatório.");
        }

        Usuario usuarioLogado = getUsuarioLogado();
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado com ID: " + grupoId));
        validarPertencimentoAoGrupo(usuarioLogado.getId(), grupo);

        List<RelatorioGastoUsuarioDTO> resultados = gastoRepository.relatorioPorUsuarioEGrupo(grupoId, inicio, fim, tipoDeGastoId);

        BigDecimal totalGeral = resultados.stream()
                .map(RelatorioGastoUsuarioDTO::valorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalGeral.compareTo(BigDecimal.ZERO) == 0) {
            return List.of();
        }

        return resultados.stream()
                .map(item -> {
                    double porcentagem = item.valorTotal()
                            .multiply(BigDecimal.valueOf(100))
                            .divide(totalGeral, 2, RoundingMode.HALF_UP)
                            .doubleValue();
                    return new RelatorioGastoUsuarioDTO(item.usuario(), item.valorTotal(), porcentagem);
                })
                .collect(Collectors.toList());
    }
}