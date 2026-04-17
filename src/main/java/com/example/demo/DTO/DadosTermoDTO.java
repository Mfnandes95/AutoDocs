package com.example.demo.DTO;

import com.example.demo.domain.model.DadosTermo;

public class DadosTermoDTO {

    private String id;
    private String nomeColaborador;
    private String dataInicio;
    private String dataTermino;
    private String idTemplate;
    private String idOrgao; 

    public DadosTermoDTO() { 
    }
    public DadosTermoDTO(String id, String nomeColaborador, String dataInicio, String dataTermino, String idTemplate, String idOrgao) {
        this.id = id;
        this.nomeColaborador = nomeColaborador;
        this.dataInicio = dataInicio;
        this.dataTermino = dataTermino;
        this.idTemplate = idTemplate;
        this.idOrgao = idOrgao;
    }
static public DadosTermoDTO fromModel(DadosTermo model) {
        return new DadosTermoDTO(
            model.getId(),
            model.getNomeColaborador(),
            model.getDataInicio(),
            model.getDataTermino(),
            model.getIdTemplate(),
            model.getIdOrgao()
        );
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getNomeColaborador() {
        return nomeColaborador;
    }
    public void setNomeColaborador(String nomeColaborador) {
        this.nomeColaborador = nomeColaborador;
    }

    public String getDataInicio() {
        return dataInicio;
    }
    public void setDataInicio(String dataInicio) {
        this.dataInicio = dataInicio;
    }

    public String getDataTermino() {
        return dataTermino;
    }
    public void setDataTermino(String dataTermino) {
        this.dataTermino = dataTermino;
    }
    public String getIdTemplate() {
        return idTemplate;
    }
    public void setIdTemplate(String idTemplate) {
        this.idTemplate = idTemplate;
    }
    public String getIdOrgao() {
        return idOrgao;
    }
    public void setIdOrgao(String idOrgao) {    
        this.idOrgao = idOrgao;
    }
}