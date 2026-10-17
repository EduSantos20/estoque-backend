package com.loja.estoque.controller;

import com.loja.estoque.dto.PontoDtos.*;
import com.loja.estoque.service.PontoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ponto")
@RequiredArgsConstructor
public class PontoController {

    private final PontoService pontoService;

    // ---------- funcionario (qualquer usuario habilitado a bater ponto) ----------

    @GetMapping("/status")
    public StatusDTO status(Authentication auth) {
        return pontoService.status(auth.getName());
    }

    @PostMapping("/bater")
    public StatusDTO bater(Authentication auth) {
        return pontoService.bater(auth.getName());
    }

    @GetMapping("/meu")
    public EspelhoDTO meu(@RequestParam(required = false) String mes, Authentication auth) {
        return pontoService.meuEspelho(auth.getName(), mes);
    }

    // ---------- administrador ----------

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/dia")
    public ResumoDiaDTO dia(@RequestParam(required = false) String data) {
        return pontoService.resumoDia(data);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/mes")
    public ResumoMesDTO mes(@RequestParam(required = false) String mes) {
        return pontoService.resumoMes(mes);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/usuarios/{id}")
    public EspelhoDTO espelhoUsuario(@PathVariable Long id, @RequestParam(required = false) String mes) {
        return pontoService.espelhoDoUsuario(id, mes);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/usuarios/{id}/batidas")
    public BatidaDTO adicionar(@PathVariable Long id, @RequestBody BatidaRequest req, Authentication auth) {
        return pontoService.adicionarBatida(id, req, auth.getName());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/batidas/{id}")
    public BatidaDTO editar(@PathVariable Long id, @RequestBody BatidaRequest req, Authentication auth) {
        return pontoService.editarBatida(id, req, auth.getName());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/batidas/{id}/remover")
    public ResponseEntity<Void> remover(@PathVariable Long id, @RequestBody RemoverBatidaRequest req, Authentication auth) {
        pontoService.removerBatida(id, req.justificativa(), auth.getName());
        return ResponseEntity.noContent().build();
    }
}
