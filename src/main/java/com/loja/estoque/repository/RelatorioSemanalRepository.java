package com.loja.estoque.repository;

import com.loja.estoque.model.RelatorioSemanal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RelatorioSemanalRepository extends JpaRepository<RelatorioSemanal, Long> {
    List<RelatorioSemanal> findAllByOrderByFechadoEmDesc();
}
