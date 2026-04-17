package com.example.demo.domain.ports.in;

import com.example.demo.domain.model.DadosTermo;

public interface GerarDocumentoUseCase {
    String executar(DadosTermo dadosDTO);
    byte[] gerarArquivoBinario(DadosTermo dadosDTO); 
}