package com.loja.estoque.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "usuarios_estoque")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    @Column(name = "nome_completo", length = 120)
    private String nomeCompleto;

    @Column(nullable = false)
    private String senha; // hash BCrypt

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    // ---------------- Ponto digital ----------------

    /** Se true, o usuario bate ponto e aparece no controle de ponto do administrador. */
    @Column(name = "controla_ponto", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean controlaPonto = false;

    @Column(name = "hora_entrada")
    private LocalTime horaEntrada;

    @Column(name = "hora_saida")
    private LocalTime horaSaida;

    /** Minutos de tolerancia para atraso/saida antecipada (padrao 10). */
    @Column(name = "tolerancia_minutos")
    private Integer toleranciaMinutos;

    /** Intervalo (almoco) descontado automaticamente quando o dia tem so entrada e saida. */
    @Column(name = "intervalo_minutos")
    private Integer intervaloMinutos;

    /** Dias de trabalho no padrao ISO separados por virgula: 1=seg ... 7=dom. Ex: "1,2,3,4,5,6". */
    @Column(name = "dias_trabalho", length = 20)
    private String diasTrabalho;

    @Column(name = "salario_mensal", precision = 12, scale = 2)
    private BigDecimal salarioMensal;

    /** Primeiro dia em que o ponto passa a valer (evita contar faltas antes de o usuario existir no controle). */
    @Column(name = "ponto_desde")
    private LocalDate pontoDesde;
}
