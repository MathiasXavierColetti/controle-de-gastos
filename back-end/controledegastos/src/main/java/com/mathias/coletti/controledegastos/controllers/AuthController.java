package com.mathias.coletti.controledegastos.controllers;

import com.mathias.coletti.controledegastos.dtos.LoginDTO;
import com.mathias.coletti.controledegastos.dtos.TokenResponseDTO;
import com.mathias.coletti.controledegastos.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid LoginDTO dto) {
        TokenResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(response); // HTTP 200 OK
    }
}