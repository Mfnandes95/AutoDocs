package com.example.demo.domain.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
    
    // Padronização: se usou JsonProperty em um, use na nomenclatura de todos se estiver alterando o padrão camelCase
    @JsonProperty("nome_colaborador")
    private String nomeColaborador;

    @JsonProperty("data_inicio")
    private String dataInicio;

    @JsonProperty("data_termino")
    private String dataTermino;

    @JsonProperty("id_template")
    private String idTemplate;
    
    @JsonProperty("id_orgao")
    private String idOrgao;

    private String patrimonio;
    private String unidade;
    private String tipo;
    private String info;

    /**
     * Mapeia o DTO para o Model de forma null-safe e exception-safe.
     * Idealmente, isso deveria estar em uma classe 'DadosTermoMapper'.
     */
    public static DadosTermo toModel(DadosTermoDTO dto) {
        if (dto == null) {
            return null;
        }

        DadosTermo model = new DadosTermo();
        model.setId(dto.getId());
        model.setNomeColaborador(dto.getNomeColaborador());
        model.setPatrimonio(dto.getPatrimonio());
        model.setUnidade(dto.getUnidade());
        model.setTipo(dto.getTipo());
        model.setInfo(dto.getInfo());
        model.setIdTemplate(dto.getIdTemplate());
        model.setIdOrgao(dto.getIdOrgao());

        // Parse seguro das datas
        model.setDataInicio(parseDataSegura(dto.getDataInicio()));
        model.setDataTermino(parseDataSegura(dto.getDataTermino()));

        return model;
    }

    private static LocalDateTime parseDataSegura(String dataStr) {
        if (dataStr == null || dataStr.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dataStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            // Em vez de explodir um erro 500 para o usuário, retorna null 
            // ou loga um warning. Depende da sua regra de negócio.
            return null; 
        }
    }
}