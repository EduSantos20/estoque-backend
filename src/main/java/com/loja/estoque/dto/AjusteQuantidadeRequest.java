package com.loja.estoque.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Usado para os botoes + / - de Estoque e A caminho.
 * quantidade pode ser positiva (+) ou negativa (-).
 */
@Data
public class AjusteQuantidadeRequest {

    @NotNull(message = "Informe a quantidade")
    private Integer quantidade;
}
