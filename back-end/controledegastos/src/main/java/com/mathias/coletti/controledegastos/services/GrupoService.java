package com.mathias.coletti.controledegastos.services;

import com.mathias.coletti.controledegastos.dtos.AdicionarMembroDTO;
import com.mathias.coletti.controledegastos.dtos.GrupoCriacaoDTO;
import com.mathias.coletti.controledegastos.dtos.GrupoResponseDTO;
import com.mathias.coletti.controledegastos.dtos.UsuarioResponseDTO;
import com.mathias.coletti.controledegastos.exceptions.AccessDeniedException;
import com.mathias.coletti.controledegastos.exceptions.BusinessException;
import com.mathias.coletti.controledegastos.exceptions.ResourceNotFoundException;
import com.mathias.coletti.controledegastos.models.Grupo;
import com.mathias.coletti.controledegastos.models.Usuario;
import com.mathias.coletti.controledegastos.repositories.GrupoRepository;
import com.mathias.coletti.controledegastos.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public GrupoResponseDTO criarGrupo(Long usuarioId, GrupoCriacaoDTO dto) {
        Usuario criador = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        Grupo grupo = new Grupo();
        grupo.setNome(dto.nome());
        grupo.setDescricao(dto.descricao());
        grupo.getUsuarios().add(criador);

        grupo = grupoRepository.save(grupo);
        return toDTO(grupo);
    }

    @Transactional
    public GrupoResponseDTO adicionarMembroPorCpf(Long usuarioAutenticadoId, Long grupoId, AdicionarMembroDTO dto) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado com o ID: " + grupoId));

        validarPertencimentoAoGrupo(usuarioAutenticadoId, grupo);

        String cpfLimpo = dto.cpf().replaceAll("\\D", "");
        Usuario novoMembro = usuarioRepository.findByPessoaCpf(cpfLimpo)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum usuário encontrado com o CPF informado."));

        if (grupo.getUsuarios().contains(novoMembro)) {
            throw new BusinessException("O usuário já faz parte deste grupo.");
        }

        grupo.getUsuarios().add(novoMembro);
        grupo = grupoRepository.save(grupo);

        return toDTO(grupo);
    }

    @Transactional(readOnly = true)
    public List<GrupoResponseDTO> listarGruposDoUsuario(Long usuarioId) {
        // Nota: Certifique-se de que no GrupoRepository existe o método findByUsuariosId(usuarioId)
        List<Grupo> grupos = grupoRepository.findByUsuariosId(usuarioId);
        return grupos.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public GrupoResponseDTO buscarPorId(Long usuarioAutenticadoId, Long grupoId) {
        Grupo grupo = grupoRepository.findById(grupoId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado com o ID: " + grupoId));

        validarPertencimentoAoGrupo(usuarioAutenticadoId, grupo);

        return toDTO(grupo);
    }

    private void validarPertencimentoAoGrupo(Long usuarioId, Grupo grupo) {
        boolean pertence = grupo.getUsuarios().stream()
                .anyMatch(usuario -> usuario.getId().equals(usuarioId));

        if (!pertence) {
            throw new AccessDeniedException("Você não tem permissão para acessar ou modificar este grupo.");
        }
    }

    private GrupoResponseDTO toDTO(Grupo grupo) {
        List<UsuarioResponseDTO> usuariosDTO = grupo.getUsuarios().stream()
                .map(u -> new UsuarioResponseDTO(
                        u.getId(),
                        u.getPessoa().getNome(),
                        u.getPessoa().getCpf()))
                .toList();

        return new GrupoResponseDTO(grupo.getId(), grupo.getNome(), grupo.getDescricao(), usuariosDTO);
    }
}