package com.example.demo.domain.dto;

import jakarta.validation.constraints.NotBlank;

public class ItemTermoDTO {

    @NotBlank(message = "O número do patrimônio é obrigatório.")
    private String patrimonio;
    
    private String equipamento;

    public String getPatrimonio() { return patrimonio; }
    public void setPatrimonio(String patrimonio) { this.patrimonio = patrimonio; }

    public String getEquipamento() { return equipamento; }
    public void setEquipamento(String equipamento) { this.equipamento = equipamento; }
}