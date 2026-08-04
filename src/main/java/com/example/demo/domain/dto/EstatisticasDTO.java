package com.example.demo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstatisticasDTO {
    
    private Long totalTermosGerados;
    
    private Long termosGeradosMesAtual;
    
    // Mapeia a quantidade de documentos gerados por tipo (Ex: "Contrato de Trabalho" -> 150)
    private Map<String, Long> termosPorCategoria;
    
    // Ajuda a monitorar a saúde e performance da aplicação
    private Double tempoMedioProcessamentoMs;
    
    // Retorna a quantidade de falhas ou tentativas de geração malsucedidas
    private Integer errosDeGeracaoMesAtual;
}