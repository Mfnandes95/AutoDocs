package com.example.demo.domain.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Column(nullable = false)
    private String senha;

    // CORREÇÃO CRÍTICA: Força o JPA a ler/gravar o Enum como texto (VARCHAR)
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UsuarioRole role;
}