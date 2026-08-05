package com.example.demo.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class TermoRequestDTO {

    @NotBlank(message = "O nome do colaborador é obrigatório.")
    private String nomeColaborador;

    @NotNull(message = "A data de início é obrigatória.")
    private LocalDate dataInicio;

    @NotNull(message = "A data de término é obrigatória.")
    private LocalDate dataTermino;

    @Valid
    private List<ItemTermoDTO> itens;

    private String info;
    private String unidade;
    private String tipo;
    private String patrimonio;

    // Getters e Setters
    public String getNomeColaborador() { return nomeColaborador; }
    public void setNomeColaborador(String nomeColaborador) { this.nomeColaborador = nomeColaborador; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataTermino() { return dataTermino; }
    public void setDataTermino(LocalDate dataTermino) { this.dataTermino = dataTermino; }

    public List<ItemTermoDTO> getItens() { return itens; }
    public void setItens(List<ItemTermoDTO> itens) { this.itens = itens; }

    public String getInfo() { return info; }
    public void setInfo(String info) { this.info = info; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getPatrimonio() { return patrimonio; }
    public void setPatrimonio(String patrimonio) { this.patrimonio = patrimonio; }
}