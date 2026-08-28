package com.loja.estoque.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MetaRequest {

    @NotNull(message = "Informe a nova meta")
    @Min(value = 0, message = "A meta nao pode ser negativa")
    private Integer meta;
}
