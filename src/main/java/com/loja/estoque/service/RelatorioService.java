package com.loja.estoque.service;

import com.loja.estoque.model.EstoqueTamanho;
import com.loja.estoque.model.RelatorioDetalhe;
import com.loja.estoque.model.RelatorioSemanal;
import com.loja.estoque.repository.EstoqueTamanhoRepository;
import com.loja.estoque.repository.RelatorioSemanalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final EstoqueTamanhoRepository estoqueTamanhoRepository;
    private final RelatorioSemanalRepository relatorioSemanalRepository;

    public List<RelatorioSemanal> listarHistorico() {
        return relatorioSemanalRepository.findAllByOrderByFechadoEmDesc();
    }

    public RelatorioSemanal buscarPorId(Long id) {
        return relatorioSemanalRepository.findById(id)
                .orElseThrow(() -> new com.loja.estoque.exception.NegocioException("Relatorio nao encontrado"));
    }

    /**
     * Fecha a semana atual:
     * - grava uma "foto" das vendas, estoque, meta e compra sugerida de cada tamanho
     * - zera o contador de vendas da semana de cada tamanho, para comecar do zero
     */
    @Transactional
    public RelatorioSemanal fecharSemana(String usuarioResponsavel, LocalDate inicioSemana) {
        List<EstoqueTamanho> linhas = estoqueTamanhoRepository.findAllByOrderByTamanhoAsc();

        int totalVendas = linhas.stream().mapToInt(EstoqueTamanho::getVendasSemana).sum();

        RelatorioSemanal relatorio = RelatorioSemanal.builder()
                .inicioSemana(inicioSemana)
                .fimSemana(LocalDate.now())
                .fechadoEm(LocalDateTime.now())
                .fechadoPor(usuarioResponsavel)
                .totalVendas(totalVendas)
                .build();

        for (EstoqueTamanho linha : linhas) {
            RelatorioDetalhe detalhe = RelatorioDetalhe.builder()
                    .relatorio(relatorio)
                    .tamanho(linha.getTamanho())
                    .vendas(linha.getVendasSemana())
                    .estoqueFinal(linha.getEstoque())
                    .meta(linha.getMeta())
                    .compraSugerida(linha.getCompraSugerida())
                    .build();
            relatorio.getDetalhes().add(detalhe);

            // zera as vendas da semana para recomecar
            linha.setVendasSemana(0);
        }

        estoqueTamanhoRepository.saveAll(linhas);
        return relatorioSemanalRepository.save(relatorio);
    }
}
