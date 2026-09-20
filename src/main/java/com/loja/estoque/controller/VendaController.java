package com.loja.estoque.controller;

import com.loja.estoque.dto.CancelarVendaRequest;
import com.loja.estoque.dto.VendaDTO;
import com.loja.estoque.model.Categoria;
import com.loja.estoque.model.Venda;
import com.loja.estoque.repository.VendaRepository;
import com.loja.estoque.service.EstoqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Historico de vendas individuais, com o usuario responsavel por cada uma,
 * e o cancelamento (estorno) de vendas.
 * Somente ADMIN pode consultar e cancelar (auditoria).
 */
@RestController
@RequestMapping("/api/vendas")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class VendaController {

    private final VendaRepository vendaRepository;
    private final EstoqueService estoqueService;

    @GetMapping
    public List<VendaDTO> listar(
            @RequestParam(required = false) Categoria categoria,
            @RequestParam(required = false) String usuario
    ) {
        List<Venda> vendas;

        if (categoria != null) {
            vendas = vendaRepository.findByCategoriaOrderByDataHoraDesc(categoria);
        } else if (usuario != null && !usuario.isBlank()) {
            vendas = vendaRepository.findByUsuarioOrderByDataHoraDesc(usuario);
        } else {
            vendas = vendaRepository.findAllByOrderByDataHoraDesc();
        }

        return vendas.stream().map(VendaDTO::fromEntity).toList();
    }

    /**
     * Cancela uma venda: devolve a quantidade ao estoque e marca o registro
     * como cancelado no historico (o registro nao e apagado).
     */
    @PostMapping("/{id}/cancelar")
    public VendaDTO cancelar(@PathVariable Long id,
                              @Valid @RequestBody(required = false) CancelarVendaRequest request,
                              Authentication authentication) {
        String motivo = request != null ? request.getMotivo() : null;
        Venda cancelada = estoqueService.cancelarVenda(id, authentication.getName(), motivo);
        return VendaDTO.fromEntity(cancelada);
    }
}
