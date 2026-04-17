package com.example.demo.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DadosTermo {

    private String id;
    private String nomeColaborador;
    private String tipo;
    private String info;
    private String dataInicio;
    private String dataTermino;
    private String idTemplate;
    private String idOrgao;

}