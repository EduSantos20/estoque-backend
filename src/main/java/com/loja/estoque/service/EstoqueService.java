package com.loja.estoque.service;

import com.loja.estoque.exception.NegocioException;
import com.loja.estoque.model.Categoria;
import com.loja.estoque.model.EstoqueTamanho;
import com.loja.estoque.model.Venda;
import com.loja.estoque.repository.EstoqueTamanhoRepository;
import com.loja.estoque.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final EstoqueTamanhoRepository repository;
    private final VendaRepository vendaRepository;

    /** Lista todos os tamanhos de uma categoria especifica (Campo, Futsal ou Society). */
    public List<EstoqueTamanho> listarPorCategoria(Categoria categoria) {
        return repository.findByCategoriaOrderByTamanhoAsc(categoria);
    }

    /** Lista tudo, de todas as categorias (usado internamente no fechamento semanal). */
    public List<EstoqueTamanho> listarTodos() {
        return repository.findAllByOrderByCategoriaAscTamanhoAsc();
    }

    private EstoqueTamanho buscarOuFalhar(Categoria categoria, Integer tamanho) {
        return repository.findByCategoriaAndTamanho(categoria, tamanho)
                .orElseThrow(() -> new NegocioException(
                        "Tamanho " + tamanho + " nao existe na categoria " + categoria.getLabel()));
    }

    /**
     * Ajusta o estoque (+ ou -). Usado apenas por usuarios autorizados (ADMIN).
     * Nao permite ficar negativo.
     */
    @Transactional
    public EstoqueTamanho ajustarEstoque(Categoria categoria, Integer tamanho, int delta) {
        EstoqueTamanho linha = buscarOuFalhar(categoria, tamanho);
        int novoValor = linha.getEstoque() + delta;
        if (novoValor < 0) {
            throw new NegocioException("Estoque do tamanho " + tamanho + " nao pode ficar negativo");
        }
        linha.setEstoque(novoValor);
        return repository.save(linha);
    }

    /**
     * Ajusta a quantidade "a caminho" (+ ou -). Usado apenas por usuarios autorizados (ADMIN),
     * normalmente atraves da confirmacao de um pedido novo (ver "Fazer Pedido" no frontend).
     */
    @Transactional
    public EstoqueTamanho ajustarACaminho(Categoria categoria, Integer tamanho, int delta) {
        EstoqueTamanho linha = buscarOuFalhar(categoria, tamanho);
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
    public EstoqueTamanho receberEncomenda(Categoria categoria, Integer tamanho, int quantidade) {
        EstoqueTamanho linha = buscarOuFalhar(categoria, tamanho);
        if (quantidade > linha.getACaminho()) {
            throw new NegocioException(
                    "Nao e possivel receber " + quantidade + " pares: apenas " + linha.getACaminho() + " estao a caminho para o tamanho " + tamanho);
        }
        linha.setACaminho(linha.getACaminho() - quantidade);
        linha.setEstoque(linha.getEstoque() + quantidade);
        return repository.save(linha);
    }

    /**
     * Registra uma venda: diminui do estoque, soma nas vendas da semana
     * e grava um registro permanente de QUEM fez a venda (para historico/auditoria).
     * Pode ser feito por qualquer usuario logado.
     */
    @Transactional
    public EstoqueTamanho registrarVenda(Categoria categoria, Integer tamanho, int quantidade, String usuario) {
        EstoqueTamanho linha = buscarOuFalhar(categoria, tamanho);
        if (quantidade > linha.getEstoque()) {
            throw new NegocioException(
                    "Estoque insuficiente do tamanho " + tamanho + ". Disponivel: " + linha.getEstoque());
        }
        linha.setEstoque(linha.getEstoque() - quantidade);
        linha.setVendasSemana(linha.getVendasSemana() + quantidade);
        EstoqueTamanho salvo = repository.save(linha);

        Venda venda = Venda.builder()
                .categoria(categoria)
                .tamanho(tamanho)
                .quantidade(quantidade)
                .usuario(usuario)
                .dataHora(LocalDateTime.now())
                .build();
        vendaRepository.save(venda);

        return salvo;
    }

    /**
     * Altera a meta fixa de um tamanho. Usado apenas por usuarios autorizados (ADMIN).
     */
    @Transactional
    public EstoqueTamanho alterarMeta(Categoria categoria, Integer tamanho, int novaMeta) {
        if (novaMeta < 0) {
            throw new NegocioException("A meta nao pode ser negativa");
        }
        EstoqueTamanho linha = buscarOuFalhar(categoria, tamanho);
        linha.setMeta(novaMeta);
        return repository.save(linha);
    }
}
