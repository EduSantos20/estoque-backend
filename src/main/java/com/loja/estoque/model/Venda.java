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
}
