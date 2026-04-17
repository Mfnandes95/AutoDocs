package com.example.demo.domain.model;

public class Template {
    
    private String id;
    private String nomeTemplate;
    private String descricaoTemplate;
    private String conteudoTemplate;

    public Template(String id, String nomeTemplate, String descricaoTemplate, String conteudoTemplate){
        this.id = id;
        this.nomeTemplate = nomeTemplate;
        this.descricaoTemplate = descricaoTemplate;
        this.conteudoTemplate = conteudoTemplate;
    }

    public String getId() {
        return id;
    }

    public String getNomeTemplate() {
        return nomeTemplate;
    }

    public String getDescricaoTemplate() {
        return descricaoTemplate;
    }

    public String getConteudoTemplate() {
        return conteudoTemplate;
    }
}