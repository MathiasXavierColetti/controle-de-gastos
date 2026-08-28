package com.mathias.coletti.controledegastos.repositories;

import com.mathias.coletti.controledegastos.models.Grupo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    // Faz um JOIN FETCH para carregar os usuários e evitar consultas extras (N+1)
    @Query("SELECT DISTINCT g FROM Grupo g LEFT JOIN FETCH g.usuarios u LEFT JOIN FETCH u.pessoa p WHERE u.id = :usuarioId")
    List<Grupo> findByUsuariosId(@Param("usuarioId") Long usuarioId);

    @Override
    @EntityGraph(attributePaths = {"usuarios", "usuarios.pessoa"})
    Optional<Grupo> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"usuarios", "usuarios.pessoa"})
    List<Grupo> findAll();
}
