package com.loja.estoque.repository;

import com.loja.estoque.model.Categoria;
import com.loja.estoque.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendaRepository extends JpaRepository<Venda, Long> {
    List<Venda> findAllByOrderByDataHoraDesc();
    List<Venda> findByCategoriaOrderByDataHoraDesc(Categoria categoria);
    List<Venda> findByUsuarioOrderByDataHoraDesc(String usuario);
}
