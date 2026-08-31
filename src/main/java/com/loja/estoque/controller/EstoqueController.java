package com.loja.estoque.controller;

import com.loja.estoque.dto.*;
import com.loja.estoque.model.Categoria;
import com.loja.estoque.model.EstoqueTamanho;
import com.loja.estoque.service.EstoqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estoque")
@RequiredArgsConstructor
public class EstoqueController {

    private final EstoqueService estoqueService;

    /** As 3 categorias fixas de grade, para o frontend montar as abas sem precisar hardcodar. */
    @GetMapping("/categorias")
    public List<Categoria> listarCategorias() {
        return List.of(Categoria.values());
    }

    /** Lista o quadro de tamanhos 34-43 de UMA categoria (Campo, Futsal ou Society). */
    @GetMapping("/{categoria}")
    public List<EstoqueTamanhoDTO> listar(@PathVariable Categoria categoria) {
        return estoqueService.listarPorCategoria(categoria).stream()
                .map(EstoqueTamanhoDTO::fromEntity)
                .toList();
    }

    /** Registrar venda: qualquer usuario logado (ADMIN ou USER). */
    @PostMapping("/{categoria}/{tamanho}/venda")
    public EstoqueTamanhoDTO registrarVenda(@PathVariable Categoria categoria, @PathVariable Integer tamanho, @Valid @RequestBody VendaRequest request) {
        EstoqueTamanho atualizado = estoqueService.registrarVenda(categoria, tamanho, request.getQuantidade());
        return EstoqueTamanhoDTO.fromEntity(atualizado);
    }

    /** Ajustar estoque com + ou -: somente ADMIN. */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{categoria}/{tamanho}/estoque")
    public EstoqueTamanhoDTO ajustarEstoque(@PathVariable Categoria categoria, @PathVariable Integer tamanho, @Valid @RequestBody AjusteQuantidadeRequest request) {
        EstoqueTamanho atualizado = estoqueService.ajustarEstoque(categoria, tamanho, request.getQuantidade());
        return EstoqueTamanhoDTO.fromEntity(atualizado);
    }

    /**
     * Ajustar "a caminho" com + ou -: somente ADMIN.
     * Usado pelo fluxo de "Fazer Pedido" no frontend, que confirma a quantidade
     * realmente encomendada de cada tamanho (baseada na compra sugerida).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{categoria}/{tamanho}/a-caminho")
    public EstoqueTamanhoDTO ajustarACaminho(@PathVariable Categoria categoria, @PathVariable Integer tamanho, @Valid @RequestBody AjusteQuantidadeRequest request) {
        EstoqueTamanho atualizado = estoqueService.ajustarACaminho(categoria, tamanho, request.getQuantidade());
        return EstoqueTamanhoDTO.fromEntity(atualizado);
    }

    /**
     * Receber encomenda (chegou dos Correios): diminui "a caminho" e soma no estoque.
     * Somente ADMIN. Este e o UNICO fluxo do botao "Correios" no frontend agora.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{categoria}/{tamanho}/receber")
    public EstoqueTamanhoDTO receber(@PathVariable Categoria categoria, @PathVariable Integer tamanho, @Valid @RequestBody ReceberEncomendaRequest request) {
        EstoqueTamanho atualizado = estoqueService.receberEncomenda(categoria, tamanho, request.getQuantidade());
        return EstoqueTamanhoDTO.fromEntity(atualizado);
    }

    /** Alterar meta fixa de um tamanho: somente ADMIN. */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{categoria}/{tamanho}/meta")
    public EstoqueTamanhoDTO alterarMeta(@PathVariable Categoria categoria, @PathVariable Integer tamanho, @Valid @RequestBody MetaRequest request) {
        EstoqueTamanho atualizado = estoqueService.alterarMeta(categoria, tamanho, request.getMeta());
        return EstoqueTamanhoDTO.fromEntity(atualizado);
    }
}
