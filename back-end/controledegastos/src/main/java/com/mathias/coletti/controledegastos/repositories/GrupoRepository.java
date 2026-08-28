package com.mathias.coletti.controledegastos.repositories;

import com.mathias.coletti.controledegastos.models.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    List<Grupo> findByUsuariosId(Long usuarioId);
}
