package com.mathias.coletti.controledegastos.services;

import com.mathias.coletti.controledegastos.dtos.AdicionarMembroDTO;
import com.mathias.coletti.controledegastos.dtos.GrupoCriacaoDTO;
import com.mathias.coletti.controledegastos.dtos.GrupoResponseDTO;
import com.mathias.coletti.controledegastos.dtos.UsuarioResponseDTO;
import com.mathias.coletti.controledegastos.exceptions.ResourceNotFoundException;
import com.mathias.coletti.controledegastos.models.Grupo;
import com.mathias.coletti.controledegastos.models.Usuario;
import com.mathias.coletti.controledegastos.repositories.GrupoRepository;
import com.mathias.coletti.controledegastos.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public GrupoResponseDTO criarGrupo(GrupoCriacaoDTO dto) {
        Usuario criador = obterUsuarioLogado();
        return criarGrupo(criador.getId(), dto);
    }

    @Transactional
    public GrupoResponseDTO criarGrupo(Long usuarioCriadorId, GrupoCriacaoDTO dto) {
        Usuario criador = usuarioRepository.findById(usuarioCriadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário criador não encontrado com ID: " + usuarioCriadorId));

        Grupo grupo = Grupo.builder()
                .nome(dto.nome())
                .descricao(dto.descricao())
                .usuarios(new HashSet<>())
                .build();

        grupo.getUsuarios().add(criador);
        criador.getGrupos().add(grupo);

        Grupo grupoSalvo = grupoRepository.save(grupo);
        return converterParaDTO(grupoSalvo);
    }

    @Transactional
    public GrupoResponseDTO atualizarGrupo(Long usuarioAutenticadoId, Long grupoId, GrupoCriacaoDTO dto) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado com o ID: " + grupoId));

        validarPertencimentoAoGrupo(usuarioAutenticadoId, grupo);

        grupo.setNome(dto.nome());
        grupo.setDescricao(dto.descricao());

        Grupo grupoAtualizado = grupoRepository.save(grupo);
        return converterParaDTO(grupoAtualizado);
    }

    @Transactional
    public void deletarGrupo(Long usuarioAutenticadoId, Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado com o ID: " + grupoId));

        validarPertencimentoAoGrupo(usuarioAutenticadoId, grupo);

        for (Usuario usuario : grupo.getUsuarios()) {
            usuario.getGrupos().remove(grupo);
        }
        grupo.getUsuarios().clear();

        grupoRepository.delete(grupo);
    }

    @Transactional
    public GrupoResponseDTO adicionarMembroPorCpf(Long usuarioAutenticadoId, Long grupoId, AdicionarMembroDTO dto) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado com o ID: " + grupoId));

        validarPertencimentoAoGrupo(usuarioAutenticadoId, grupo);

        String cpfLimpo = dto.cpf().replaceAll("\\D", "");

        Usuario novoMembro = usuarioRepository.findByPessoaCpf(dto.cpf())
                .or(() -> usuarioRepository.findByPessoaCpf(cpfLimpo))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com o CPF informado não foi encontrado."));

        if (grupo.getUsuarios().contains(novoMembro)) {
            throw new IllegalArgumentException("Este usuário já faz parte do grupo.");
        }

        grupo.getUsuarios().add(novoMembro);
        novoMembro.getGrupos().add(grupo);

        Grupo grupoAtualizado = grupoRepository.save(grupo);
        return converterParaDTO(grupoAtualizado);
    }

    @Transactional
    public GrupoResponseDTO removerMembro(Long usuarioAutenticadoId, Long grupoId, Long membroId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado."));

        validarPertencimentoAoGrupo(usuarioAutenticadoId, grupo);

        Usuario usuarioParaRemover = usuarioRepository.findById(membroId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário/Membro não encontrado."));

        grupo.getUsuarios().remove(usuarioParaRemover);
        usuarioParaRemover.getGrupos().remove(grupo);

        Grupo grupoAtualizado = grupoRepository.save(grupo);
        return converterParaDTO(grupoAtualizado);
    }

    @Transactional(readOnly = true)
    public List<GrupoResponseDTO> listarGruposDoUsuario(Long usuarioId) {
        List<Grupo> grupos = grupoRepository.findByUsuariosId(usuarioId);
        return grupos.stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public GrupoResponseDTO buscarPorId(Long usuarioAutenticadoId, Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado com o ID: " + grupoId));

        validarPertencimentoAoGrupo(usuarioAutenticadoId, grupo);

        return converterParaDTO(grupo);
    }

    @Transactional(readOnly = true)
    public List<GrupoResponseDTO> listarGruposDoUsuarioLogado() {
        Usuario usuario = obterUsuarioLogado();
        List<Grupo> grupos = grupoRepository.findByUsuariosId(usuario.getId());

        return grupos.stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GrupoResponseDTO> listarTodos() {
        List<Grupo> grupos = grupoRepository.findAll();
        return grupos.stream()
                .map(this::converterParaDTO)
                .toList();
    }

    /**
     * Recupera o usuário logado convertendo diretamente o 'sub' do token (auth.getName()) para Long,
     * uma vez que o token armazena o ID do usuário no subject.
     */
    public Usuario obterUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não está autenticado.");
        }

        Object principal = auth.getPrincipal();
        Long usuarioId = null;

        // 1. Caso o principal já seja do tipo Long (ex: gerado por um filtro customizado)
        if (principal instanceof Long id) {
            usuarioId = id;
        }
        // 2. Caso o principal seja do tipo String (ex: "1" vindo de um JWT comum)
        else if (principal instanceof String str) {
            try {
                usuarioId = Long.parseLong(str);
            } catch (NumberFormatException ignored) {
                // Se for CPF em formato String
                String cpfLimpo = str.replaceAll("\\D", "");
                return usuarioRepository.findByPessoaCpf(str)
                        .or(() -> usuarioRepository.findByPessoaCpf(cpfLimpo))
                        .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado para o CPF: " + str));
            }
        }
        // 3. Fallback usando auth.getName() sem fazer cast forçado de String
        else if (auth.getName() != null) {
            String name = auth.getName();
            try {
                usuarioId = Long.parseLong(name);
            } catch (NumberFormatException e) {
                String cpfLimpo = name.replaceAll("\\D", "");
                return usuarioRepository.findByPessoaCpf(name)
                        .or(() -> usuarioRepository.findByPessoaCpf(cpfLimpo))
                        .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + name));
            }
        }

        // Se identificamos o ID do usuário, faz a busca direta por ID
        if (usuarioId != null) {
            Long idBusca = usuarioId;
            return usuarioRepository.findById(idBusca)
                    .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + idBusca));
        }

        throw new AccessDeniedException("Não foi possível identificar o usuário no token de autenticação.");
    }

    private void validarPertencimentoAoGrupo(Long usuarioId, Grupo grupo) {
        boolean pertence = grupo.getUsuarios().stream()
                .anyMatch(u -> u.getId().equals(usuarioId));

        if (!pertence) {
            throw new AccessDeniedException("Acesso negado: O usuário não pertence a este grupo.");
        }
    }

    private GrupoResponseDTO converterParaDTO(Grupo grupo) {
        List<UsuarioResponseDTO> membrosDTO = grupo.getUsuarios().stream()
                .map(u -> new UsuarioResponseDTO(
                        u.getId(),
                        u.getPessoa() != null ? u.getPessoa().getNome() : null,
                        u.getPessoa() != null ? u.getPessoa().getCpf() : null
                ))
                .collect(Collectors.toList());

        return new GrupoResponseDTO(
                grupo.getId(),
                grupo.getNome(),
                grupo.getDescricao(),
                membrosDTO
        );
    }
}