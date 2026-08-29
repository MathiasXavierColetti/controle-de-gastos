package com.mathias.coletti.controledegastos.services;

import com.mathias.coletti.controledegastos.dtos.UsuarioAtualizacaoDTO;
import com.mathias.coletti.controledegastos.dtos.UsuarioCadastroDTO;
import com.mathias.coletti.controledegastos.dtos.UsuarioResponseDTO;
import com.mathias.coletti.controledegastos.exceptions.BusinessException;
import com.mathias.coletti.controledegastos.exceptions.ResourceNotFoundException;
import com.mathias.coletti.controledegastos.models.Pessoa;
import com.mathias.coletti.controledegastos.models.Usuario;
import com.mathias.coletti.controledegastos.repositories.PessoaRepository;
import com.mathias.coletti.controledegastos.repositories.UsuarioRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PessoaRepository pessoaRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponseDTO cadastrar(UsuarioCadastroDTO dto) {
        String cpfLimpo = dto.cpf().replaceAll("\\D", "");

        // Validação de CPF único
        if (pessoaRepository.existsByCpf(cpfLimpo)) {
            throw new BusinessException("Já existe um usuário cadastrado com o CPF informado.");
        }

        // Criar Pessoa
        Pessoa pessoa = new Pessoa();
        pessoa.setNome(dto.nome());
        pessoa.setCpf(cpfLimpo);
        pessoa = pessoaRepository.save(pessoa);

        // Criar Usuário associado à Pessoa
        Usuario usuario = new Usuario();
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuario.setPessoa(pessoa);
        usuario = usuarioRepository.save(usuario);

        return new UsuarioResponseDTO(
                usuario.getId(),
                pessoa.getNome(),
                pessoa.getCpf()
        );
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(usuario -> new UsuarioResponseDTO(
                        usuario.getId(),
                        usuario.getPessoa().getNome(),
                        usuario.getPessoa().getCpf()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID: " + id));

        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getPessoa().getNome(),
                usuario.getPessoa().getCpf()
        );
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, @NotNull UsuarioAtualizacaoDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        // Atualiza nome e cpf através do objeto Pessoa relacionado
        if (dto.nome() != null && !dto.nome().isBlank()) {
            usuario.getPessoa().setNome(dto.nome());
        }

        if (dto.usuario() != null && !dto.usuario().isBlank()) {
            usuario.getPessoa().setCpf(dto.usuario());
        }

        // Lógica para alterar a senha (se preenchida)
        if (dto.novaSenha() != null && !dto.novaSenha().isBlank()) {
            if (dto.senhaAtual() == null || !passwordEncoder.matches(dto.senhaAtual(), usuario.getSenha())) {
                throw new BusinessException("A senha atual está incorreta.");
            }
            if (dto.novaSenha().length() < 6) {
                throw new BusinessException("A nova senha deve ter no mínimo 6 caracteres.");
            }
            usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        }

        usuarioRepository.save(usuario);
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getPessoa().getNome(),
                usuario.getPessoa().getCpf()
        );
    }

    @Transactional
    public void resetarSenha(Long id, String novaSenha) {
        if (novaSenha == null || novaSenha.isBlank()) {
            throw new BusinessException("A nova senha não pode ser vazia.");
        }

        if (novaSenha.length() < 6) {
            throw new BusinessException("A nova senha deve ter no mínimo 6 caracteres.");
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID: " + id));

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void deletar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID: " + id));

        Pessoa pessoa = usuario.getPessoa();

        usuarioRepository.delete(usuario);
        if (pessoa != null) {
            pessoaRepository.delete(pessoa);
        }
    }
}