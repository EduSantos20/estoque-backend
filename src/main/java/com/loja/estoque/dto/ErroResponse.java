package com.loja.estoque.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErroResponse {
    private LocalDateTime timestamp;
    private int status;
    private String mensagem;
}
