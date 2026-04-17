package com.example.demo.domain.ports.out;

public interface ArmazemPort {
    String guardar(byte[] conteudo, String nomeArquivo);
}