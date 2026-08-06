package com.example.demo.infrastructure.repository;

import com.example.demo.domain.model.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UsuarioEntity, Long> {

    // Case-insensitive: e-mail é normalizado (trim + lowercase) na escrita,
    // mas a busca também ignora caixa para tolerar registros antigos e
    // pequenas variações de digitação sem quebrar o login.
    Optional<UsuarioEntity> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}