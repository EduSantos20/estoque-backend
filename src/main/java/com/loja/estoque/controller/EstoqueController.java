package com.loja.estoque.controller;

import com.loja.estoque.dto.*;
import com.loja.estoque.model.EstoqueTamanho;
import com.loja.estoque.service.EstoqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estoque")
@RequiredArgsConstructor
public class EstoqueController {

    private final EstoqueService estoqueService;

    /** Lista o quadro de tamanhos 34-43 com estoque, vendas, a caminho, meta e compra sugerida. */
    @GetMapping
    public List<EstoqueTamanhoDTO> listar() {
        return estoqueService.listarTodos().stream()
                .map(EstoqueTamanhoDTO::fromEntity)
                .toList();
    }

    /** Registrar venda: qualquer usuario logado (ADMIN ou USER). */
    @PostMapping("/{tamanho}/venda")
    public EstoqueTamanhoDTO registrarVenda(@PathVariable Integer tamanho, @Valid @RequestBody VendaRequest request) {
        EstoqueTamanho atualizado = estoqueService.registrarVenda(tamanho, request.getQuantidade());
        return EstoqueTamanhoDTO.fromEntity(atualizado);
    }

    /** Ajustar estoque com + ou -: somente ADMIN. */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{tamanho}/estoque")
    public EstoqueTamanhoDTO ajustarEstoque(@PathVariable Integer tamanho, @Valid @RequestBody AjusteQuantidadeRequest request) {
        EstoqueTamanho atualizado = estoqueService.ajustarEstoque(tamanho, request.getQuantidade());
        return EstoqueTamanhoDTO.fromEntity(atualizado);
    }

    /** Ajustar "a caminho" com + ou - (registrar compra): somente ADMIN. */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{tamanho}/a-caminho")
    public EstoqueTamanhoDTO ajustarACaminho(@PathVariable Integer tamanho, @Valid @RequestBody AjusteQuantidadeRequest request) {
        EstoqueTamanho atualizado = estoqueService.ajustarACaminho(tamanho, request.getQuantidade());
        return EstoqueTamanhoDTO.fromEntity(atualizado);
    }

    /** Receber encomenda (chegou dos Correios): diminui "a caminho" e soma no estoque. Somente ADMIN. */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{tamanho}/receber")
    public EstoqueTamanhoDTO receber(@PathVariable Integer tamanho, @Valid @RequestBody ReceberEncomendaRequest request) {
        EstoqueTamanho atualizado = estoqueService.receberEncomenda(tamanho, request.getQuantidade());
        return EstoqueTamanhoDTO.fromEntity(atualizado);
    }

    /** Alterar meta fixa de um tamanho: somente ADMIN. */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{tamanho}/meta")
    public EstoqueTamanhoDTO alterarMeta(@PathVariable Integer tamanho, @Valid @RequestBody MetaRequest request) {
        EstoqueTamanho atualizado = estoqueService.alterarMeta(tamanho, request.getMeta());
        return EstoqueTamanhoDTO.fromEntity(atualizado);
    }
}
