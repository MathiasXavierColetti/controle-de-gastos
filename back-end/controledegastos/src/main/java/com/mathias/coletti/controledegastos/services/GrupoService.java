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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Cria um novo grupo utilizando o usuário logado na sessão como criador.
     */
    @Transactional
    public GrupoResponseDTO criarGrupo(GrupoCriacaoDTO dto) {
        Usuario criador = obterUsuarioLogado();
        return criarGrupo(criador.getId(), dto);
    }

    /**
     * Cria um grupo associando o usuário criador.
     */
    @Transactional
    public GrupoResponseDTO criarGrupo(Long usuarioCriadorId, GrupoCriacaoDTO dto) {
        Usuario criador = usuarioRepository.findById(usuarioCriadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário criador não encontrado com ID: " + usuarioCriadorId));

        Grupo grupo = Grupo.builder()
                .nome(dto.nome())
                .descricao(dto.descricao())
                .usuarios(new HashSet<>())
                .build();

        // Adiciona o criador à lista de usuários do grupo
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

        // Remove os vínculos da tabela intermediária antes de deletar
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

        Usuario novoMembro = usuarioRepository.findByPessoaCpf(cpfLimpo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com o CPF informado não foi encontrado."));

        if (grupo.getUsuarios().contains(novoMembro)) {
            throw new IllegalArgumentException("Este usuário já faz parte do grupo.");
        }

        grupo.getUsuarios().add(novoMembro);
        novoMembro.getGrupos().add(grupo); // Mantém a consistência bidirecional

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
     * Recupera a entidade Usuario referente ao usuário atualmente autenticado via JWT (por CPF).
     */
    public Usuario obterUsuarioLogado() {
        org.springframework.security.core.Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Usuário não está autenticado.");
        }

        String cpf = auth.getName(); // Método padrão e seguro para obter a identificação do usuário
        String cpfLimpo = cpf.replaceAll("\\D", "");

        // Busca primeiro pelo CPF exatamente como veio no token, e se não achar, busca pelo CPF sem pontuação
        return usuarioRepository.findByPessoaCpf(cpf)
                .or(() -> usuarioRepository.findByPessoaCpf(cpfLimpo))
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado para o CPF: " + cpf));
    }

    private void validarPertencimentoAoGrupo(Long usuarioId, Grupo grupo) {
        boolean pertence = grupo.getUsuarios().stream()
                .anyMatch(u -> u.getId().equals(usuarioId));


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