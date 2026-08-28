package com.loja.estoque.dto;

import com.loja.estoque.model.Role;
import com.loja.estoque.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsuarioDTO {
    private Long id;
    private String username;
    private String nomeCompleto;
    private Role role;
    private boolean ativo;

    public static UsuarioDTO fromEntity(Usuario u) {
        return new UsuarioDTO(u.getId(), u.getUsername(), u.getNomeCompleto(), u.getRole(), u.isAtivo());
    }
}
