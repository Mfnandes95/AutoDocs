package com.example.demo.domain.service;

import com.example.demo.domain.model.DadosTermo;
import com.example.demo.domain.ports.out.DocsGerar;
import com.example.demo.domain.ports.out.ArmazemPort;
import com.example.demo.domain.ports.in.GerarDocumentoUseCase;
import org.springframework.stereotype.Service;

@Service
public class DocsService implements GerarDocumentoUseCase {

    private final DocsGerar geradorPort;
    private final ArmazemPort armazenamentoPort;

    public DocsService(DocsGerar geradorPort, ArmazemPort armazenamentoPort) {
        this.geradorPort = geradorPort;
        this.armazenamentoPort = armazenamentoPort;
    }

    @Override
    public String executar(DadosTermo dados) {
        byte[] arquivoDocx = gerarArquivoBinario(dados);
        return armazenamentoPort.guardar(arquivoDocx, dados.getNomeColaborador() + "-termo.docx");
    }

    @Override
    public byte[] gerarArquivoBinario(DadosTermo dados) {
    // Certifique-se de que o 'geradorPort.gerar' realmente devolve o conteúdo do arquivo
    byte[] binario = geradorPort.gerar(dados, "Template.docx");
    
    if (binario == null || binario.length == 0) {
        System.err.println("ERRO: O gerador devolveu um arquivo vazio!");
    }
    
    return binario;
}
}