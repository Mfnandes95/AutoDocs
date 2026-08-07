package com.example.demo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDTO {

    private int totalLinhas;
    private int importados;
    private int ignorados;
    private List<LinhaComErro> erros;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LinhaComErro {
        private int linha; // número da linha na planilha (1-based, contando o cabeçalho como linha 1)
        private String motivo;
    }
}
