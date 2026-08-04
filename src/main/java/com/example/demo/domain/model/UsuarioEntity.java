package com.example.demo.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    // CORREÇÃO: O hash da senha é explicitamente ocultado de logs e comparações
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    private UsuarioRole role;
}