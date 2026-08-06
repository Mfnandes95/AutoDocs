package com.example.demo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    // Reutiliza o DTO de estatísticas para os totais
    private EstatisticasDTO estatisticasGerais;
    
    // Retorna apenas os últimos 5 ou 10 termos gerados para popular a tela inicial
    private List<DadosTermoDTO> atividadesRecentes;
    
    // (Opcional) Pode conter dados para gerar um gráfico de barras no front-end
    // Ex: "2026-05" -> 45 termos, "2026-06" -> 60 termos
    private List<GraficoEvolucaoDTO> graficoEvolucaoMensal;

    // Quantidade de termos ativos agrupados por unidade/escritório (gráfico "por escritório")
    private Map<String, Long> porUnidade;

    // Quantidade de termos ativos agrupados por tipo de equipamento (gráfico "por categoria")
    private Map<String, Long> porTipo;

    // Termos cuja dataTermino cai nos próximos 7 dias (KPI "a vencer")
    private Long totalAVencer;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GraficoEvolucaoDTO {
        private String mesAno;
        private Long quantidade;
    }
}