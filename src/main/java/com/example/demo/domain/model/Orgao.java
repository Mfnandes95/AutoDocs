package com.example.demo.domain.model;

import java.util.List;
import java.util.ArrayList;

public class Orgao {
    
    private String nomeOrgao;
    private List<UsuarioEntity> colaboradores;
    private String emailDaUnidade;
    private String termosAtivos;

    public Orgao(String nomeOrgao, List<UsuarioEntity> colaboradores, String emailDaUnidade, String termosAtivos){
        this.nomeOrgao = nomeOrgao;
        this.emailDaUnidade = emailDaUnidade;
        this.termosAtivos = termosAtivos;
        this.colaboradores = (colaboradores != null) ? colaboradores : new ArrayList<>();
    }

    public String getNomeOrgao() {
        return nomeOrgao;
    }   

    public List<UsuarioEntity> getColaboradores() {
        return colaboradores;
    }

    public String getEmailDaUnidade() {
        return emailDaUnidade;
    }

    public String getTermosAtivos() {
        return termosAtivos;
    }
}
