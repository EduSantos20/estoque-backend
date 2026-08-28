package com.loja.estoque.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "relatorio_semanal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatorioSemanal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inicio_semana", nullable = false)
    private LocalDate inicioSemana;

    @Column(name = "fim_semana", nullable = false)
    private LocalDate fimSemana;

    @Column(name = "fechado_em", nullable = false)
    private LocalDateTime fechadoEm;

    @Column(name = "fechado_por", length = 60)
    private String fechadoPor;

    @Column(name = "total_vendas", nullable = false)
    private Integer totalVendas;

    @OneToMany(mappedBy = "relatorio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RelatorioDetalhe> detalhes = new ArrayList<>();
}
