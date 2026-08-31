package com.loja.estoque.repository;

import com.loja.estoque.model.Categoria;
import com.loja.estoque.model.EstoqueTamanho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstoqueTamanhoRepository extends JpaRepository<EstoqueTamanho, Long> {
    List<EstoqueTamanho> findAllByOrderByCategoriaAscTamanhoAsc();
    List<EstoqueTamanho> findByCategoriaOrderByTamanhoAsc(Categoria categoria);
    Optional<EstoqueTamanho> findByCategoriaAndTamanho(Categoria categoria, Integer tamanho);
    boolean existsByCategoriaAndTamanho(Categoria categoria, Integer tamanho);
}
