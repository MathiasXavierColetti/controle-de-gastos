package com.mathias.coletti.controledegastos.services;

import com.mathias.coletti.controledegastos.dtos.LoginDTO;
import com.mathias.coletti.controledegastos.dtos.TokenResponseDTO;
import com.mathias.coletti.controledegastos.exceptions.BusinessException;
import com.mathias.coletti.controledegastos.models.Usuario;
import com.mathias.coletti.controledegastos.repositories.UsuarioRepository;
import com.mathias.coletti.controledegastos.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenResponseDTO login(LoginDTO dto) {
        String cpfLimpo = dto.getCpf().replaceAll("\\D", "");

        Usuario usuario = usuarioRepository.findByPessoaCpf(cpfLimpo)
                .orElseThrow(() -> new BusinessException("CPF ou senha inválidos."));

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new BusinessException("CPF ou senha inválidos.");
        }

        String token = jwtTokenProvider.gerarToken(usuario);

        return new TokenResponseDTO(
                token,
                "Bearer",
                usuario.getId(),
                usuario.getPessoa().getNome()
        );
    }
}