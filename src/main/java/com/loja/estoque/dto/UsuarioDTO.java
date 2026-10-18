package com.loja.estoque.dto;

import com.loja.estoque.model.Role;
import com.loja.estoque.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class UsuarioDTO {
    private Long id;
    private String username;
    private String nomeCompleto;
    private Role role;
    private boolean ativo;
    private boolean controlaPonto;
    private LocalTime horaEntrada;
    private LocalTime horaSaida;
    private Integer toleranciaMinutos;
    private Integer intervaloMinutos;
    private String diasTrabalho;
    private BigDecimal salarioMensal;
    private LocalDate pontoDesde;

    public static UsuarioDTO fromEntity(Usuario u) {
        return new UsuarioDTO(
                u.getId(), u.getUsername(), u.getNomeCompleto(), u.getRole(), u.isAtivo(),
                u.isControlaPonto(), u.getHoraEntrada(), u.getHoraSaida(),
                u.getToleranciaMinutos(), u.getIntervaloMinutos(), u.getDiasTrabalho(),
                u.getSalarioMensal(), u.getPontoDesde()
        );
    }
}
