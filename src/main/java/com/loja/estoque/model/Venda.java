package com.loja.estoque.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Registro individual de cada venda feita, para saber quem vendeu o que e quando.
 * Diferente de EstoqueTamanho.vendasSemana (que e so um contador que zera toda
 * semana), este historico fica salvo permanentemente.
 */
@Entity
@Table(name = "venda")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Categoria categoria;

    @Column(nullable = false)
    private Integer tamanho;

    @Column(nullable = false)
    private Integer quantidade;

    /** Username de quem registrou a venda (vem do usuario logado via JWT). */
    @Column(nullable = false, length = 60)
    private String usuario;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    /**
     * Cancelamento (estorno): quando true, a venda foi cancelada e a quantidade
     * ja voltou para o estoque. O registro NAO e apagado -- fica no historico
     * marcado como cancelado, preservando a rastreabilidade de quem fez o que.
     */
    @Column(name = "cancelada", nullable = false)
    @Builder.Default
    private boolean cancelada = false;

    @Column(name = "cancelada_por", length = 60)
    private String canceladaPor;

    @Column(name = "cancelada_em")
    private LocalDateTime canceladaEm;

    @Column(name = "motivo_cancelamento", length = 255)
    private String motivoCancelamento;
}
