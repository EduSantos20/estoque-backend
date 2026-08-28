package com.loja.estoque.dto;

import com.loja.estoque.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String nomeCompleto;
    private Role role;
}
