package com.loja.estoque.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Informe o usuario")
    private String username;

    @NotBlank(message = "Informe a senha")
    private String senha;
}
