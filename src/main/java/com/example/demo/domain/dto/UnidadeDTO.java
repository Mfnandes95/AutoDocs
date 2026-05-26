package com.example.demo.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class UnidadeDTO {
    private String nome;
    private List<ResponsavelDTO> responsaveis;
}