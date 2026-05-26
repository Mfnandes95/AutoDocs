package com.example.demo.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class EscritoriosDTO {
    private String nome;        // controller usava setNomePredio — corrigido no controller
    private long totalTermos;
    private long totalAvarias;
    private List<UnidadeDTO> unidades;
}