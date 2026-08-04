package com.example.demo.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class TermoRequestDTO {
    
    @NotBlank(message = "Nome é obrigatório")
    private String nomeColaborador;
    
    @NotBlank(message = "Patrimônio é obrigatório") // Adicionado
    private String patrimonio;
    
    private String unidade;
    private String info;
    private String tipo;
    
    // O Spring valida e converte a string do JSON ("2026-03-20") para LocalDate automaticamente.
    @NotNull(message = "Data início obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataInicio;
    
    @NotNull(message = "Data término obrigatória")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataTermino;
}