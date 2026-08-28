package com.mathias.coletti.controledegastos.repositories;

import com.mathias.coletti.controledegastos.models.TipoDeGasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface TipoDeGastoRepository extends JpaRepository<TipoDeGasto, Long> {
    Optional<TipoDeGastoRepository> findByNome(String nome);

    boolean existsByNome(String nome);
    // Adicione esta linha para o Spring Data JPA reconhecer o método
    boolean existsByNomeIgnoreCase(String nome);
}
