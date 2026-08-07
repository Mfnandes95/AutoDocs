package com.example.demo.infrastructure.repository;

import com.example.demo.domain.model.InventarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<InventarioEntity, Long> {

    /**
     * Busca um ativo pelo seu número de patrimônio/tag único.
     */
    Optional<InventarioEntity> findByPatrimonio(String patrimonio);

    /**
     * Verifica a existência prévia de um patrimônio.
     */
    boolean existsByPatrimonio(String patrimonio);
}