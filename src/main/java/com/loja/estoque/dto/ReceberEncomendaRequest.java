package com.loja.estoque.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Usado quando a encomenda chega (Correios):
 * diminui "a caminho" e soma no estoque.
 */
@Data
public class ReceberEncomendaRequest {

    @NotNull(message = "Informe a quantidade recebida")
    @Min(value = 1, message = "A quantidade recebida deve ser maior que zero")
    private Integer quantidade;
}
