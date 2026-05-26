package com.example.demo.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

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

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("status_aparelho")
    private String statusAparelho;
    
    @JsonProperty("patrimonio")
    private String patrimonio;
}