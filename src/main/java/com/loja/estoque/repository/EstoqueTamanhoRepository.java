package com.loja.estoque.repository;

import com.loja.estoque.model.EstoqueTamanho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EstoqueTamanhoRepository extends JpaRepository<EstoqueTamanho, Integer> {
    List<EstoqueTamanho> findAllByOrderByTamanhoAsc();
}
