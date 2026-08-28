package com.mathias.coletti.controledegastos.repositories;

import com.mathias.coletti.controledegastos.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByPessoaCpf(String cpf);

    boolean existsByPessoaCpf(String cpf);
}
