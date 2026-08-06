package com.example.demo.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo de domínio que transporta os dados do Termo entre Service e Adapter.
 *
 * ADICIONADO: campo itensLista (List<ItemTermo>) para suportar múltiplos
 * patrimônios na tabela do documento — corrige a regressão introduzida quando
 * a geração passou a usar só o campo patrimônio concatenado como String.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DadosTermo {

    @JsonProperty("id_termo")
    private String id;

    @JsonProperty("nome_colaborador")
    private String nomeColaborador;

    @JsonProperty("tipo")
    private String tipo;

    @JsonProperty("info")
    private String info;

    @JsonProperty("data_inicio")
    private LocalDateTime dataInicio;

    @JsonProperty("data_termino")
    private LocalDateTime dataTermino;

    @JsonProperty("data_criacao")
    private LocalDateTime dataCriacao;

    @JsonProperty("id_template")
    private String idTemplate;

    @JsonProperty("id_orgao")
    private String idOrgao;

    @JsonProperty("id_unidade")
    private String unidade;

    @JsonProperty("id_equipamento")
    private String idEquipamento;

    @JsonProperty("equipamento")
    private String equipamento;

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("status_aparelho")
    private String statusAparelho;

    @JsonProperty("patrimonio")
    private String patrimonio;

    /**
     * Lista de itens (patrimônio + equipamento) para templates com tabela.
     * Quando preenchida, o PoiWordAdapter popula a tabela {{#itens}} do .docx.
     */
    @JsonProperty("itens")
    private List<ItemTermo> itensLista;

    // ──────────────────────────────────────────────────────────────
    // Classe interna — representa uma linha da tabela no template
    // ──────────────────────────────────────────────────────────────
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemTermo {
        private String patrimonio;
        private String equipamento;
    }
}
