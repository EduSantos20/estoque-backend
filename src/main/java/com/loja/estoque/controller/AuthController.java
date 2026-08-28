package com.loja.estoque.controller;

import com.loja.estoque.dto.LoginRequest;
import com.loja.estoque.dto.LoginResponse;
import com.loja.estoque.model.Usuario;
import com.loja.estoque.repository.UsuarioRepository;
import com.loja.estoque.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getSenha())
        );

        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow();

        String token = jwtUtil.gerarToken(usuario.getUsername(), usuario.getRole().name());

        return ResponseEntity.ok(new LoginResponse(
                token,
                usuario.getUsername(),
                usuario.getNomeCompleto(),
                usuario.getRole()
        ));
    }
}
