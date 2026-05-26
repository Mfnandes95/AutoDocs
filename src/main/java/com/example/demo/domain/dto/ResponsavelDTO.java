package com.example.demo.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class ResponsavelDTO {
    private String nome;
    private List<EquipamentoDTO> equipamentos;
}