package com.loja.estoque.service;

import com.loja.estoque.exception.NegocioException;
import com.loja.estoque.model.EstoqueTamanho;
import com.loja.estoque.repository.EstoqueTamanhoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final EstoqueTamanhoRepository repository;

    public List<EstoqueTamanho> listarTodos() {
        return repository.findAllByOrderByTamanhoAsc();
    }

    private EstoqueTamanho buscarOuFalhar(Integer tamanho) {
        return repository.findById(tamanho)
                .orElseThrow(() -> new NegocioException("Tamanho " + tamanho + " nao existe"));
    }

    /**
     * Ajusta o estoque (+ ou -). Usado apenas por usuarios autorizados (ADMIN).
     * Nao permite ficar negativo.
     */
    @Transactional
    public EstoqueTamanho ajustarEstoque(Integer tamanho, int delta) {
        EstoqueTamanho linha = buscarOuFalhar(tamanho);
        int novoValor = linha.getEstoque() + delta;
        if (novoValor < 0) {
            throw new NegocioException("Estoque do tamanho " + tamanho + " nao pode ficar negativo");
        }
        linha.setEstoque(novoValor);
        return repository.save(linha);
    }

    /**
     * Ajusta a quantidade "a caminho" (+ ou -). Usado apenas por usuarios autorizados (ADMIN)
     * ao registrar uma nova compra.
     */
    @Transactional
    public EstoqueTamanho ajustarACaminho(Integer tamanho, int delta) {
        EstoqueTamanho linha = buscarOuFalhar(tamanho);
        int novoValor = linha.getACaminho() + delta;
        if (novoValor < 0) {
            throw new NegocioException("Quantidade 'a caminho' do tamanho " + tamanho + " nao pode ficar negativa");
        }
        linha.setACaminho(novoValor);
        return repository.save(linha);
    }

    /**
     * Registra o recebimento de uma encomenda: diminui "a caminho" e soma no estoque.
     * Usado apenas por usuarios autorizados (ADMIN).
     */
    @Transactional
    public EstoqueTamanho receberEncomenda(Integer tamanho, int quantidade) {
        EstoqueTamanho linha = buscarOuFalhar(tamanho);
        if (quantidade > linha.getACaminho()) {
            throw new NegocioException(
                    "Nao e possivel receber " + quantidade + " pares: apenas " + linha.getACaminho() + " estao a caminho para o tamanho " + tamanho);
        }
        linha.setACaminho(linha.getACaminho() - quantidade);
        linha.setEstoque(linha.getEstoque() + quantidade);
        return repository.save(linha);
    }

    /**
     * Registra uma venda: diminui do estoque e soma nas vendas da semana.
     * Pode ser feito por qualquer usuario logado.
     */
    @Transactional
    public EstoqueTamanho registrarVenda(Integer tamanho, int quantidade) {
        EstoqueTamanho linha = buscarOuFalhar(tamanho);
        if (quantidade > linha.getEstoque()) {
            throw new NegocioException(
                    "Estoque insuficiente do tamanho " + tamanho + ". Disponivel: " + linha.getEstoque());
        }
        linha.setEstoque(linha.getEstoque() - quantidade);
        linha.setVendasSemana(linha.getVendasSemana() + quantidade);
        return repository.save(linha);
    }

    /**
     * Altera a meta fixa de um tamanho. Usado apenas por usuarios autorizados (ADMIN).
     */
    @Transactional
    public EstoqueTamanho alterarMeta(Integer tamanho, int novaMeta) {
        if (novaMeta < 0) {
            throw new NegocioException("A meta nao pode ser negativa");
        }
        EstoqueTamanho linha = buscarOuFalhar(tamanho);
        linha.setMeta(novaMeta);
        return repository.save(linha);
    }
}
