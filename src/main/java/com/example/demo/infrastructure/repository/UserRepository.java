package com.example.demo.infrastructure.repository;

import com.example.demo.domain.model.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UsuarioEntity, Long> {
    
    // Método crucial para o Spring Security encontrar o usuário no login
    Optional<UsuarioEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
