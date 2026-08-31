package com.loja.estoque.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "relatorio_detalhe")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatorioDetalhe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "relatorio_id", nullable = false)
    @JsonIgnore
    private RelatorioSemanal relatorio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Categoria categoria;

    @Column(nullable = false)
    private Integer tamanho;

    @Column(name = "vendas", nullable = false)
    private Integer vendas;

    @Column(name = "estoque_final", nullable = false)
    private Integer estoqueFinal;

    @Column(name = "meta", nullable = false)
    private Integer meta;

    @Column(name = "compra_sugerida", nullable = false)
    private Integer compraSugerida;
}
