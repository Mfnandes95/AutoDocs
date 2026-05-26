package com.example.demo.domain.model;

import jakarta.persistence.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class TermoEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "prédio")
    private String predio;

    @Column(name = "nome_colaborador")
    private String nomeColaborador;

    @Column(nullable = false)
    private String info;
    
    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private LocalDateTime dataInicio;
    
    @Column(nullable = false)
    private LocalDateTime dataTermino;

    @Column(name = "data_criacao")
    private LocalDateTime dataCriacao;

    @Column(name = "patrimonio")
    private String patrimonio;

    @Column(name = "equipamento")
    private String equipamento;

    @Column(name = "unidade")
    private String unidade;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "status_aparelho")
    private String statusAparelho;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPredio() { return predio; }
    public void setPredio(String predio) { this.predio = predio; }
    public String getNomeColaborador() { return nomeColaborador; }
    public void setNomeColaborador(String nomeColaborador) { this.nomeColaborador = nomeColaborador; }
    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public LocalDateTime getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }
    public LocalDateTime getDataTermino() { return dataTermino; }
    public void setDataTermino(LocalDateTime dataTermino) { this.dataTermino = dataTermino; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public String getPatrimonio(){ return patrimonio;}
    public void setPatrimonio(String patrimonio){ this.patrimonio = patrimonio; }
    public String getUnidade(){ return unidade;}
    public void setUnidade(String unidade){this.unidade = unidade;}
    public String getEquipamento(){ return equipamento;}
    public void setEquipamento(String equipamento){this.equipamento = equipamento;}
    public String getDescricao(){ return descricao;}
    public void setDescricao(String Descricao){this.descricao = Descricao;}
    public String getStatusAparelho(){ return statusAparelho;}
    public void setStatusAparelho(String statusAparelho){this.statusAparelho = statusAparelho;}

@jakarta.persistence.PrePersist
protected void onCreate() {
    this.dataCriacao = LocalDateTime.now();
    }
}
