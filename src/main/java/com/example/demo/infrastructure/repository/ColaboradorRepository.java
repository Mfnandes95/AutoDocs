package com.example.demo.infrastructure.repository;

import com.example.demo.domain.model.ColaboradorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColaboradorRepository extends JpaRepository<ColaboradorEntity, Long> {

    Optional<ColaboradorEntity> findByNome(String nome);

    List<ColaboradorEntity> findByNomeContainingIgnoreCase(String nome);
}
