package com.example.demo.domain.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.example.demo.domain.model.DadosTermo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DadosTermoDTO {

    private String id;

    @JsonProperty("nome_colaborador")
    private String nomeColaborador;

    @JsonProperty("data_inicio")
    private String dataInicio;

    @JsonProperty("data_termino")
    private String dataTermino;

    private String idTemplate;
    private String idOrgao;

    @JsonProperty("patrimonio")   // ← adicionado
    private String patrimonio;

    @JsonProperty("unidade")      // ← adicionado
    private String unidade;

    @JsonProperty("tipo")         // ← adicionado
    private String tipo;

    @JsonProperty("info")         // ← adicionado
    private String info;

    public static DadosTermo toModel(DadosTermoDTO dto) {
        DadosTermo model = new DadosTermo();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        model.setId(dto.getId());
        model.setNomeColaborador(dto.getNomeColaborador());
        model.setPatrimonio(dto.getPatrimonio());
        model.setUnidade(dto.getUnidade());
        model.setTipo(dto.getTipo());
        model.setInfo(dto.getInfo());

        if (dto.getDataInicio() != null && !dto.getDataInicio().isEmpty()) {
            model.setDataInicio(LocalDateTime.parse(dto.getDataInicio(), formatter));
        }
        if (dto.getDataTermino() != null && !dto.getDataTermino().isEmpty()) {
            model.setDataTermino(LocalDateTime.parse(dto.getDataTermino(), formatter));
        }

        model.setIdTemplate(dto.getIdTemplate());
        model.setIdOrgao(dto.getIdOrgao());

        return model;
    }
}