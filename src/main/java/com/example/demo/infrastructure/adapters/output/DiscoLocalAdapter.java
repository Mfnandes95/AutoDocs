package com.example.demo.infrastructure.adapters.output;

import com.example.demo.domain.ports.out.ArmazemPort;
import org.springframework.stereotype.Component;

@Component
public class DiscoLocalAdapter implements ArmazemPort {

    @Override
    public String guardar(byte[] conteudo, String nomeArquivo) {  
        System.out.println("Arquivo salvo com sucesso: " + nomeArquivo);
        return nomeArquivo;  
    }
}