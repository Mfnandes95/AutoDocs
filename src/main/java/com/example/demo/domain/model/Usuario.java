package com.example.demo.domain.model;

public class Usuario {
    
    private String nome;
    private String unidade;
    private String cargo;
    private String email;

    public Usuario(String nome, String unidade, String cargo, String email){
       if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("E-mail inválido para o sistema de automação.");
        }
        this.nome = nome;
        this.unidade = unidade;
        this.cargo = cargo;
        this.email = email;
    }
    public boolean podeAprovarDocumentos() {
    return "Gerente".equalsIgnoreCase(this.cargo);
    }

    public String getNome() {
        return nome;
    }

    public String getUnidade() {
        return unidade;
    }

    public String getCargo() {
        return cargo;
    }

    public String getEmail() {
        return email;
    }
}
