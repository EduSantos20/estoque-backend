package com.loja.estoque.controller;

import com.loja.estoque.model.RelatorioSemanal;
import com.loja.estoque.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    /** Lista o historico de relatorios semanais ja fechados (mais recente primeiro). */
    @GetMapping
    public List<RelatorioSemanal> listar() {
        return relatorioService.listarHistorico();
    }

    /** Detalhe de um relatorio especifico (para tela de impressao). */
    @GetMapping("/{id}")
    public RelatorioSemanal buscar(@PathVariable Long id) {
        return relatorioService.buscarPorId(id);
    }

    /**
     * Fecha a semana atual: salva o relatorio no historico e zera as vendas da semana.
     * Qualquer usuario logado pode fechar (ajuste com @PreAuthorize("hasRole('ADMIN')") se quiser restringir).
     */
    @PostMapping("/fechar-semana")
    public RelatorioSemanal fecharSemana(@RequestParam(required = false) String inicioSemana, Authentication authentication) {
        LocalDate inicio = inicioSemana != null ? LocalDate.parse(inicioSemana) : LocalDate.now().minusDays(7);
        return relatorioService.fecharSemana(authentication.getName(), inicio);
    }
}
