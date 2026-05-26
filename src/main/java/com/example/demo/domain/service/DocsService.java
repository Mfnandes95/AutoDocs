package com.example.demo.domain.service;

import com.example.demo.domain.model.DadosTermo;
import com.example.demo.domain.model.TermoEntity;
import com.example.demo.domain.ports.out.DocsGerar;
import com.example.demo.domain.ports.out.ArmazemPort;
import com.example.demo.domain.ports.in.GerarDocumentoUseCase;
import com.example.demo.infrastructure.repository.TermoRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Primary
@Service
public class DocsService implements GerarDocumentoUseCase {

    private final DocsGerar geradorPort;
    private final ArmazemPort armazenamentoPort;
    private final TermoRepository repository;

    public DocsService(DocsGerar geradorPort, ArmazemPort armazenamentoPort, TermoRepository repository) {
        this.geradorPort = geradorPort;
        this.armazenamentoPort = armazenamentoPort;
        this.repository = repository;
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

    @Override 
    public byte[] gerarArquivoDinamico(MultipartFile file, DadosTermo dados){
        try{
            return file.getBytes();
        }catch (Exception e){
            return new byte[0];
        }
    }

public Map<String, Long> obterResumo(){
    List<TermoEntity> todos = repository.findAll();
        return todos.stream().collect(Collectors.groupingBy(TermoEntity::getUnidade, Collectors.counting()));
}

}