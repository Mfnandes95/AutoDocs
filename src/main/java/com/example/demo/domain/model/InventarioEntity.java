package com.example.demo.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String patrimonio;

    @Column(nullable = false)
    private String nome;

    private String localizacao;

    private String status;

    public InventarioEntity(String patrimonio, String nome) {
        this.patrimonio = patrimonio;
        this.nome = nome;
    }
}