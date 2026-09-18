package com.mathias.coletti.controledegastos.services;

import com.mathias.coletti.controledegastos.dtos.GastoResponseDTO;
import com.mathias.coletti.controledegastos.dtos.RelatorioGastoUsuarioDTO;
import com.mathias.coletti.controledegastos.exceptions.ResourceNotFoundException;
import com.mathias.coletti.controledegastos.models.Gasto;
import com.mathias.coletti.controledegastos.models.Grupo;
import com.mathias.coletti.controledegastos.models.Usuario;
import com.mathias.coletti.controledegastos.repositories.GastoRepository;
import com.mathias.coletti.controledegastos.repositories.GrupoRepository;
import com.mathias.coletti.controledegastos.repositories.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Serviço focado exclusivamente em consultas de leitura
public class RelatorioService {


}