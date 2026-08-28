package com.loja.estoque.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VendaRequest {

    @NotNull(message = "Informe a quantidade vendida")
    @Min(value = 1, message = "A quantidade vendida deve ser maior que zero")
    private Integer quantidade;
}
