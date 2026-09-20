package com.loja.estoque.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelarVendaRequest {

    @Size(max = 255, message = "O motivo deve ter no maximo 255 caracteres")
    private String motivo;
}
