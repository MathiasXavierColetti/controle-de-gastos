package com.mathias.coletti.controledegastos.security;

import com.mathias.coletti.controledegastos.models.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:9a2f8c4e7b1a5d3f8e6c4b2a0f9e8d7c6b5a4f3e2d1c0b9a8f7e6d5c4b3a2f1e}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24 horas em milissegundos
    private long jwtExpirationMs;

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Sanitiza o token removendo quebras de linha e espaços extras enviados no Header
    private String limparToken(String token) {
        if (token == null) return null;
        return token.replaceAll("[\\r\\n\\s]+", "").trim();
    }

    // Gera o token contendo o ID do Usuário no Subject e informações da Pessoa nos Claims
    public String gerarToken(Usuario usuario) {
        Date agora = new Date();
        Date dataExpiracao = new Date(agora.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(usuario.getId().toString())
                .claim("cpf", usuario.getPessoa() != null ? usuario.getPessoa().getCpf() : "")
                .claim("nome", usuario.getPessoa() != null ? usuario.getPessoa().getNome() : "")
                .setIssuedAt(agora)
                .setExpiration(dataExpiracao)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Valida a assinatura e a expiração do token sem estourar exceção no servidor
    public boolean validarToken(String token) {
        try {
            String tokenLimpo = limparToken(token);
            if (tokenLimpo == null || tokenLimpo.isEmpty()) return false;

            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(tokenLimpo);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Extrai o ID do usuário guardado dentro do token
    public Long getUsuarioIdDoToken(String token) {
        Claims claims = getClaims(token);
        return claims != null ? Long.parseLong(claims.getSubject()) : null;
    }

    // Extrai o CPF guardado na claim "cpf" dentro do token
    public String getCpfDoToken(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.get("cpf", String.class) : null;
    }

    private Claims getClaims(String token) {
        try {
            String tokenLimpo = limparToken(token);
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(tokenLimpo)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    // Monta a autenticação do Spring Security atribuindo ROLE_USER para autorizar as requisições
    public Authentication getAuthentication(String token) {
        Long usuarioId = getUsuarioIdDoToken(token);
        if (usuarioId == null) return null;

        return new UsernamePasswordAuthenticationToken(
                usuarioId,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}