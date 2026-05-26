package com.example.demo.infrastructure.adapters.output;

import com.deepoove.poi.XWPFTemplate;
import com.example.demo.domain.model.DadosTermo;
import com.example.demo.domain.ports.out.DocsGerar;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Primary;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
@Primary
public class PoiWordAdapter implements DocsGerar {

    @Override
    public byte[] gerar(DadosTermo dados, String caminhoTemplate) {
        Map<String, Object> model = new HashMap<>();
        model.put("nomeColaborador", dados.getNomeColaborador());
        model.put("info", dados.getInfo());
        model.put("tipo", dados.getTipo());
        model.put("dataInicio", dados.getDataInicio());

        // O Spring vai procurar em: src/main/resources/templates/template.docx
        try {
            ClassPathResource resource = new ClassPathResource("templates/" + caminhoTemplate);
            
            try (InputStream is = resource.getInputStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                
                XWPFTemplate template = XWPFTemplate.compile(is).render(model);
                template.write(out);
                return out.toByteArray();
            }
        } catch (Exception e) {
            // ESSA LINHA É ESSENCIAL: Olhe o terminal do VS Code após o erro 500
            System.err.println("ERRO CRÍTICO NO ADAPTER: " + e.getMessage());
            e.printStackTrace(); 
            throw new RuntimeException("Falha ao gerar arquivo: " + e.getMessage());
        }
    }
}