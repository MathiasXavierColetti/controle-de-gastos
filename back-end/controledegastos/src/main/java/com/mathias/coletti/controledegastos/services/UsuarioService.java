package com.mathias.coletti.controledegastos.services;

import com.mathias.coletti.controledegastos.dtos.UsuarioCadastroDTO;
import com.mathias.coletti.controledegastos.dtos.UsuarioResponseDTO;
import com.mathias.coletti.controledegastos.exceptions.BusinessException;
import com.mathias.coletti.controledegastos.exceptions.ResourceNotFoundException;
import com.mathias.coletti.controledegastos.models.Pessoa;
import com.mathias.coletti.controledegastos.models.Usuario;
import com.mathias.coletti.controledegastos.repositories.PessoaRepository;
import com.mathias.coletti.controledegastos.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o ID: " + id));

        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getPessoa().getNome(),
                usuario.getPessoa().getCpf()
        );
    }
}