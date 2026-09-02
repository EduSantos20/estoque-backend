package com.loja.estoque.controller;

import com.loja.estoque.dto.VendaDTO;
import com.loja.estoque.model.Categoria;
import com.loja.estoque.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Historico de vendas individuais, com o usuario responsavel por cada uma.
 * Somente ADMIN pode consultar (auditoria).
 */
@RestController
@RequestMapping("/api/vendas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class VendaController {

    private final VendaRepository vendaRepository;

    @GetMapping
    public List<VendaDTO> listar(
            @RequestParam(required = false) Categoria categoria,
            @RequestParam(required = false) String usuario
    ) {
        List<com.loja.estoque.model.Venda> vendas;

        if (categoria != null) {
            vendas = vendaRepository.findByCategoriaOrderByDataHoraDesc(categoria);
        } else if (usuario != null && !usuario.isBlank()) {
            vendas = vendaRepository.findByUsuarioOrderByDataHoraDesc(usuario);
        } else {
            vendas = vendaRepository.findAllByOrderByDataHoraDesc();
        }

        return vendas.stream().map(VendaDTO::fromEntity).toList();
    }
}
