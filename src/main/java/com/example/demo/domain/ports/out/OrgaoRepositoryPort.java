package com.example.demo.domain.ports.out;

import com.example.demo.domain.model.Orgao;
import java.util.List;
import java.util.Optional;

public interface OrgaoRepositoryPort {
    
    void salvarOrgao(Orgao orgao);
    Optional<Orgao> buscarOrgaoPorNome(String nomeOrgao);
    List<Orgao> listarOrgaos();
    
}
