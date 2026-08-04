package com.example.demo.infrastructure.repository;

import com.example.demo.domain.model.Equipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {
    List<Equipamento> findByNomeContainingIgnoreCase(String nome);
}