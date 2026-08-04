package com.example.demo.domain.ports.out;

import com.example.demo.domain.model.DadosTermo;

public interface DocsGerar {
    byte[] gerar(DadosTermo dados, String caminhoTemplate);

    byte[] gerar(DadosTermo dados, java.io.InputStream templateStream);
}