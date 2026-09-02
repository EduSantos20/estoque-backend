package com.loja.estoque.dto;

import com.loja.estoque.model.Categoria;
import com.loja.estoque.model.Venda;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class VendaDTO {
    private Long id;
    private Categoria categoria;
    private Integer tamanho;
    private Integer quantidade;
    private String usuario;
    private LocalDateTime dataHora;

    public static VendaDTO fromEntity(Venda v) {
        return new VendaDTO(v.getId(), v.getCategoria(), v.getTamanho(), v.getQuantidade(), v.getUsuario(), v.getDataHora());
    }
}
