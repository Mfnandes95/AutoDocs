package com.example.demo.infrastructure.adapters.output;

import com.deepoove.poi.XWPFTemplate;
import com.example.demo.domain.model.DadosTermo;
import com.example.demo.domain.ports.out.DocsGerar;
import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


@Component
public class PoiWordAdapter implements DocsGerar { 
   
    @Override
    public byte[] gerar(DadosTermo dados, String caminhoTemplate){
        Map<String, Object> model = new HashMap<>();
        model.put("nomeColaborador", dados.getNomeColaborador());
        model.put("dataInicio", dados.getDataInicio());
        model.put("dataTermino", dados.getDataTermino());
        model.put("idTemplate", dados.getIdTemplate());
        model.put("idOrgao", dados.getIdOrgao());

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("templates/" + caminhoTemplate);
         ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        
        if (is == null) {
            throw new RuntimeException("Arquivo template não encontrado em resources/templates/" + caminhoTemplate);
        }

        XWPFTemplate template = XWPFTemplate.compile(is).render(model);
        template.write(out);
        return out.toByteArray();
        
    } catch (Exception e) {
        e.printStackTrace(); // Isso vai mostrar o erro real no seu terminal
        throw new RuntimeException("Erro ao processar o Word: " + e.getMessage());
    }
}
}