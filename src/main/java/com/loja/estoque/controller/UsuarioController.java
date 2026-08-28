package com.loja.estoque.controller;

import com.loja.estoque.dto.CriarUsuarioRequest;
import com.loja.estoque.dto.UsuarioDTO;
import com.loja.estoque.model.Usuario;
import com.loja.estoque.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Gestao de usuarios do sistema. Todas as acoes aqui sao restritas ao ADMIN. */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioDTO> listar() {
        return usuarioService.listar().stream().map(UsuarioDTO::fromEntity).toList();
    }

    @PostMapping
    public UsuarioDTO criar(@Valid @RequestBody CriarUsuarioRequest request) {
        Usuario usuario = usuarioService.criar(request);
        return UsuarioDTO.fromEntity(usuario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        usuarioService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
