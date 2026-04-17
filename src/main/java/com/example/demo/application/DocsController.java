package com.example.demo.application;

import com.example.demo.domain.model.DadosTermo;
import com.example.demo.domain.ports.in.GerarDocumentoUseCase;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/docs")
@CrossOrigin(origins = "*") 
public class DocsController {

    private final GerarDocumentoUseCase useCase;

    public DocsController(GerarDocumentoUseCase useCase) {
        this.useCase = useCase;
    }

    // Mantemos APENAS este método para o download
    @PostMapping(value = "/gerar")
    public ResponseEntity<Resource> download(@RequestBody DadosTermo dados) {
        
        // 1. Obtém os bytes do arquivo
        byte[] arquivo = useCase.gerarArquivoBinario(dados);
        
        // 2. Cria o recurso de retorno
        ByteArrayResource resource = new ByteArrayResource(arquivo);

        // 3. Define o nome do arquivo limpo (sem espaços)
        String nomeColaborador = dados.getNomeColaborador() != null ? dados.getNomeColaborador() : "colaborador";
        String filename = "Termo_" + nomeColaborador.replaceAll("\\s+", "_") + ".docx";

        // 4. Monta a resposta com os headers de download
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .contentLength(arquivo.length)
                .body(resource);
    }
}