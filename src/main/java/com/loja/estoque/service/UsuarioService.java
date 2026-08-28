package com.loja.estoque.service;

import com.loja.estoque.dto.CriarUsuarioRequest;
import com.loja.estoque.exception.NegocioException;
import com.loja.estoque.model.Usuario;
import com.loja.estoque.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public Usuario criar(CriarUsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new NegocioException("Ja existe um usuario com este username");
        }
        Usuario usuario = Usuario.builder()
                .username(request.getUsername())
                .nomeCompleto(request.getNomeCompleto())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(request.getRole())
                .ativo(true)
                .build();
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void desativar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Usuario nao encontrado"));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }
}
