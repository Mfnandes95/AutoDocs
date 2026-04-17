package com.example.demo.domain.model;

import java.util.List;
import java.util.ArrayList;

public class Orgao {
    
    private String nomeOrgao;
    private List<Usuario> colaboradores;
    private String emailDaUnidade;
    private String termosAtivos;

    public Orgao(String nomeOrgao, List<Usuario> colaboradores, String emailDaUnidade, String termosAtivos){
        this.nomeOrgao = nomeOrgao;
        this.emailDaUnidade = emailDaUnidade;
        this.termosAtivos = termosAtivos;
        this.colaboradores = (colaboradores != null) ? colaboradores : new ArrayList<>();
    }

    public String getNomeOrgao() {
        return nomeOrgao;
    }   

    public List<Usuario> getColaboradores() {
        return colaboradores;
    }

    public String getEmailDaUnidade() {
        return emailDaUnidade;
    }

    public String getTermosAtivos() {
        return termosAtivos;
    }
}
