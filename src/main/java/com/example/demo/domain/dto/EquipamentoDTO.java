package com.example.demo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipamentoDTO {
    private String nome;
    private String statusAparelho;
    private String patrimonio;
    private String descricao;
}