package com.example.demo.domain.dto;

public class EquipamentoDTO {
    private String nome;
    private String statusAparelho;
    private String patrimonio;
    private String descricao;

    public EquipamentoDTO(){}

    public EquipamentoDTO(String nome, String statusAparelho) {
        this.nome = nome;
        this.statusAparelho = statusAparelho;
    }
    
    public String getNome() {
        return nome;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getStatusAparelho() {
        return statusAparelho;
    }
      public void setStatusAparelho(String statusAparelho) {
        this.statusAparelho = statusAparelho;
    }

    public String getPatrimonio() {
        return patrimonio;
    }
    public void setPatrimonio(String patrimonio) {
        this.patrimonio = patrimonio;
    }

    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

}