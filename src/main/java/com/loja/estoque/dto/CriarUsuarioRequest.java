package com.loja.estoque.dto;

import com.loja.estoque.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CriarUsuarioRequest {

    @NotBlank(message = "Informe o usuario")
    private String username;

    private String nomeCompleto;

    @NotBlank(message = "Informe a senha")
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
    private String senha;

    @NotNull(message = "Informe o perfil (ADMIN ou USER)")
    private Role role;
}
