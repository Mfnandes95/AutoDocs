package com.example.demo.domain.ports.in;

import com.example.demo.domain.dto.TermoRequestDTO;
import com.example.demo.domain.model.DadosTermo;
import org.springframework.web.multipart.MultipartFile;

public interface GerarDocumentoUseCase {
    String executar(DadosTermo dadosDTO);
    byte[] gerarArquivoBinario(DadosTermo dadosDTO); 
    byte[] gerarArquivoDinamico(MultipartFile file, DadosTermo dados);
    byte[] processarGeracao(MultipartFile file, TermoRequestDTO dto);
}