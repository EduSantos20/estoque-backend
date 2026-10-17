package com.loja.estoque.repository;

import com.loja.estoque.model.Ponto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PontoRepository extends JpaRepository<Ponto, Long> {

    List<Ponto> findByUsuarioIdAndRemovidaFalseAndDataHoraBetweenOrderByDataHoraAsc(
            Long usuarioId, LocalDateTime inicio, LocalDateTime fim);

    List<Ponto> findByRemovidaFalseAndDataHoraBetweenOrderByDataHoraAsc(
            LocalDateTime inicio, LocalDateTime fim);

    Optional<Ponto> findFirstByUsuarioIdAndRemovidaFalseOrderByDataHoraDesc(Long usuarioId);
}
