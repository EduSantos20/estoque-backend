package com.loja.estoque.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Uma linha fixa por tamanho (34 a 43).
 * As linhas sao criadas uma unica vez (seed) e nunca sao removidas,
 * apenas seus valores numericos sao atualizados.
 */
@Entity
@Table(name = "estoque_tamanho")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstoqueTamanho {

    @Id
    @Column(name = "tamanho")
    private Integer tamanho; // 34 a 43, fixo - é a propria chave primaria

    @Column(nullable = false)
    @Builder.Default
    private Integer estoque = 0; // quantidade atual em estoque

    @Column(name = "vendas_semana", nullable = false)
    @Builder.Default
    private Integer vendasSemana = 0; // zera toda vez que a semana e fechada

    @Column(name = "a_caminho", nullable = false)
    @Builder.Default
    private Integer aCaminho = 0; // comprado, ainda nao recebido

    @Column(nullable = false)
    @Builder.Default
    private Integer meta = 10; // fixo em 10, so ADMIN altera

    /**
     * Compra sugerida da semana = max(meta - estoque - aCaminho, 0)
     * Calculado dinamicamente, nao fica salvo no banco.
     */
    @Transient
    public int getCompraSugerida() {
        int sugerido = meta - estoque - aCaminho;
        return Math.max(sugerido, 0);
    }
}
