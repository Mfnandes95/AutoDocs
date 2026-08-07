package com.example.demo.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Colaborador importado por planilha, usado para preencher o campo
 * "Nome do Colaborador / Responsável" na geração de termos por seleção
 * em vez de digitação livre.
 *
 * MVP: só o nome é usado de fato hoje. Setor/matrícula ficam como
 * colunas opcionais já suportadas na importação, prontas pra quando o
 * fluxo evoluir pra puxar de AD/RH — a ideia é que a origem dos dados
 * (planilha hoje, AD/RH depois) mude sem precisar mexer no restante do
 * sistema, que só enxerga esta tabela.
 */
@Entity
@Table(name = "tb_colaborador")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColaboradorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    private String matricula;

    private String setor;

    public ColaboradorEntity(String nome) {
        this.nome = nome;
    }
}
