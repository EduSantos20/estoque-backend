package com.loja.estoque.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Edicao de usuario existente. Campos nulos = mantem o valor atual. Senha vazia = mantem a senha. */
@Data
public class AtualizarUsuarioRequest {

    private String nomeCompleto;
    private String senha;

    private Boolean controlaPonto;
    private LocalTime horaEntrada;
    private LocalTime horaSaida;
    private Integer toleranciaMinutos;
    private Integer intervaloMinutos;
    private String diasTrabalho;
    private BigDecimal salarioMensal;
    private LocalDate pontoDesde;
}
