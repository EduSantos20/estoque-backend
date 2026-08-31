package com.loja.estoque.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.loja.estoque.model.Categoria;
import com.loja.estoque.model.EstoqueTamanho;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EstoqueTamanhoDTO {
    private Categoria categoria;
    private Integer tamanho;
    private Integer estoque;
    private Integer vendasSemana;

    // @JsonProperty explicito: sem isso, o Jackson serializaria este campo
    // como "ACaminho" (letra unica maiuscula + maiuscula seguinte confunde
    // a introspeccao padrao de beans). Isso garante que o JSON sempre use
    // "aCaminho", como o frontend espera.
    @JsonProperty("aCaminho")
    private Integer aCaminho;

    private Integer meta;
    private Integer compraSugerida;

    public static EstoqueTamanhoDTO fromEntity(EstoqueTamanho e) {
        return new EstoqueTamanhoDTO(
                e.getCategoria(),
                e.getTamanho(),
                e.getEstoque(),
                e.getVendasSemana(),
                e.getACaminho(),
                e.getMeta(),
                e.getCompraSugerida()
        );
    }
}
