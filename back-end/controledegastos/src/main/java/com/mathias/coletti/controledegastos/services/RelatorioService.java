package com.mathias.coletti.controledegastos.services;

import com.mathias.coletti.controledegastos.dtos.GastoResponseDTO;
import com.mathias.coletti.controledegastos.dtos.RelatorioGastoUsuarioDTO;
import com.mathias.coletti.controledegastos.exceptions.ResourceNotFoundException;
import com.mathias.coletti.controledegastos.models.Gasto;
import com.mathias.coletti.controledegastos.models.Grupo;
import com.mathias.coletti.controledegastos.models.Usuario;
import com.mathias.coletti.controledegastos.repositories.GastoRepository;
import com.mathias.coletti.controledegastos.repositories.GrupoRepository;
import com.mathias.coletti.controledegastos.repositories.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Serviço focado exclusivamente em consultas de leitura
public class RelatorioService {

    private final GastoRepository gastoRepository;
    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Extrai o Usuário da sessão com dupla verificação por CPF (1º Tentativa direta / 2º Tentativa via ID -> CPF).
     */
    private Usuario getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não autenticado.");
        }

        Object principal = authentication.getPrincipal();
        String identificadorSessao;

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

        if (!cpfLimpo.isEmpty()) {
            return usuarioRepository.findByPessoaCpf(cpfLimpo)
                    .orElseGet(() -> buscarUsuarioPorOutroIdentificadorESegundaConsultaCpf(identificadorSessao));
        }

        return buscarUsuarioPorOutroIdentificadorESegundaConsultaCpf(identificadorSessao);
    }

    private Usuario buscarUsuarioPorOutroIdentificadorESegundaConsultaCpf(String identificadorSessao) {
        Usuario usuarioTemp;
        try {
            Long usuarioId = Long.parseLong(identificadorSessao);
            usuarioTemp = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado para o ID: " + usuarioId));
        } catch (NumberFormatException e) {
            throw new ResourceNotFoundException("Usuário não encontrado para o identificador da sessão: " + identificadorSessao);
        }

        if (usuarioTemp.getPessoa() == null || usuarioTemp.getPessoa().getCpf() == null) {
            throw new ResourceNotFoundException("Usuário encontrado, mas não possui CPF vinculado.");
        }

        String cpfExtraiDoUsuario = usuarioTemp.getPessoa().getCpf().replaceAll("\\D", "");

        return usuarioRepository.findByPessoaCpf(cpfExtraiDoUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado na consulta por CPF: " + cpfExtraiDoUsuario));
    }

    private void validarPertencimentoAoGrupo(Long usuarioId, Grupo grupo) {
        boolean pertence = grupo.getUsuarios().stream()
                .anyMatch(u -> u.getId().equals(usuarioId));

        if (!pertence) {
            throw new AccessDeniedException("Usuário não possui acesso ao grupo informado.");
        }
    }

    /**
     * Consulta detalhada dinâmica de Gastos (Filtra por Grupo, Usuário, Tipo e Período dinamicamente)
     */
    public List<GastoResponseDTO> consultarRelatorioDinamico(
            Long grupoId,
            Long usuarioFiltroId,
            Long tipoDeGastoId,
            LocalDate inicio,
            LocalDate fim) {

        Usuario usuarioLogado = getUsuarioLogado();

        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado com ID: " + grupoId));

        validarPertencimentoAoGrupo(usuarioLogado.getId(), grupo);

        // Monta a especificação dinâmica com JPA Criteria
        Specification<Gasto> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Obrigatório: Grupo
            predicates.add(cb.equal(root.get("grupo").get("id"), grupoId));

            // 2. Dinâmico: Filtro por usuário específico (se enviado)
            if (usuarioFiltroId != null) {
                predicates.add(cb.equal(root.get("usuario").get("id"), usuarioFiltroId));
            }

            // 3. Dinâmico: Filtro por Tipo de Gasto (se enviado)
            if (tipoDeGastoId != null) {
                predicates.add(cb.equal(root.get("tipoDeGasto").get("id"), tipoDeGastoId));
            }

            // 4. Dinâmico: Filtro por Período
            if (inicio != null && fim != null) {
                predicates.add(cb.between(root.get("data"), inicio, fim));
            } else if (inicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("data"), inicio));
            } else if (fim != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("data"), fim));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Gasto> gastos = gastoRepository.findAll((Sort) spec);

        return gastos.stream()
                .map(this::converterParaDTO)
                .toList();
    }

    /**
     * Relatório Agrupado / Gráfico Pizza
     */
    public List<RelatorioGastoUsuarioDTO> obterRelatorioPizza(
            Long grupoId,
            LocalDate inicio,
            LocalDate fim,
            Long tipoDeGastoId) {

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