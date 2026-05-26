package com.example.demo.infrastructure.repository;

import com.example.demo.domain.model.TermoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TermoRepository extends JpaRepository<TermoEntity, Long> {
    
    List<TermoEntity> findByUnidade(String unidade);

    TermoEntity findByPatrimonio(String patrimonio);

    List<TermoEntity> findByStatusAparelho(String statusAparelho);

    List<TermoEntity> findByNomeColaborador(String nomeColaborador);

    long countByUnidade(String unidade);

    long countByStatusAparelho(String statusAparelho);
}