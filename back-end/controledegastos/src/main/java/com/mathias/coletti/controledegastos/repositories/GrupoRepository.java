package com.mathias.coletti.controledegastos.repositories;

import com.mathias.coletti.controledegastos.models.Grupo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {

    /**
     * Busca os grupos nos quais o usuário está cadastrado, carregando os membros
     * e suas respectivas Pessoas em uma única consulta (evita problema de N+1 e LazyInitializationException).
     */
    @Query("SELECT DISTINCT g FROM Grupo g " +
            "LEFT JOIN FETCH g.usuarios u " +
            "LEFT JOIN FETCH u.pessoa " +
            "WHERE u.id = :usuarioId")
    List<Grupo> findByUsuariosId(@Param("usuarioId") Long usuarioId);

    /**
     * Busca um Grupo por ID carregando ansiosamente (Eager) o relacionamento de usuários e pessoas.
     */
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"usuarios", "usuarios.pessoa"})
    Optional<Grupo> findById(@NonNull Long id);

    /**
     * Lista todos os Grupos carregando ansiosamente os relacionamentos de usuários e pessoas.
     */
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"usuarios", "usuarios.pessoa"})
    List<Grupo> findAll();
}