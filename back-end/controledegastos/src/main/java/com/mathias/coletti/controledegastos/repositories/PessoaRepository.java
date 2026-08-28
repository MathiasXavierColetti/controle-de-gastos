package com.mathias.coletti.controledegastos.repositories;

import com.mathias.coletti.controledegastos.models.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, Long> {
    Optional<Pessoa> findByCpf(String cpf);

    boolean existsByCpf(String cpf);
}
