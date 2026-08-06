package com.example.demo.infrastructure.adapters.output;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.data.Rows;
import com.example.demo.domain.model.DadosTermo;
import com.example.demo.domain.ports.out.DocsGerar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Primary
public class PoiWordAdapter implements DocsGerar {

    private static final Logger logger = LoggerFactory.getLogger(PoiWordAdapter.class);
    private static final DateTimeFormatter FORMATTER_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public byte[] gerar(DadosTermo dados, String caminhoTemplate) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/" + caminhoTemplate);
            try (InputStream is = resource.getInputStream()) {
                return gerarInterno(dados, is);
            }
        } catch (Exception e) {
            logger.error("Erro ao gerar documento a partir do classpath ({}): {}", caminhoTemplate, e.getMessage(), e);
            throw new RuntimeException("Falha ao gerar arquivo Word: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] gerar(DadosTermo dados, InputStream templateStream) {
        try (InputStream closableStream = templateStream) {
            return gerarInterno(dados, closableStream);
        } catch (Exception e) {
            logger.error("Erro ao gerar documento a partir do InputStream: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao gerar arquivo Word: " + e.getMessage(), e);
        }
    }

    private byte[] gerarInterno(DadosTermo dados, InputStream templateStream) throws Exception {
        Map<String, Object> model = construirModel(dados);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFTemplate template = XWPFTemplate.compile(templateStream).render(model);
            template.write(out);
            template.close();
            return out.toByteArray();
        }
    }

    private Map<String, Object> construirModel(DadosTermo dados) {
        Map<String, Object> model = new HashMap<>();

        model.put("nomeColaborador", nvl(dados.getNomeColaborador()));
        model.put("tipo",            nvl(dados.getTipo()));
        model.put("info",            nvl(dados.getInfo()));
        model.put("unidade",         nvl(dados.getUnidade()));
        model.put("patrimonio",      nvl(dados.getPatrimonio()));
        model.put("descricao",       nvl(dados.getDescricao()));
        model.put("statusAparelho",  nvl(dados.getStatusAparelho()));

        model.put("dataInicio",  dados.getDataInicio()  != null
                ? dados.getDataInicio().format(FORMATTER_BR)  : "");
        model.put("dataTermino", dados.getDataTermino() != null
                ? dados.getDataTermino().format(FORMATTER_BR) : "");
        model.put("dataCriacao", dados.getDataCriacao() != null
                ? dados.getDataCriacao().format(FORMATTER_BR) : "");

        if (dados.getItensLista() != null && !dados.getItensLista().isEmpty()) {
            List<RowRenderData> linhas = dados.getItensLista().stream()
                    .map(item -> Rows.of(
                            nvl(item.getPatrimonio()),
                            nvl(item.getEquipamento())
                    ).create())
                    .toList();
            model.put("itens", linhas);
        } else {
            model.put("itens", List.of());
        }

        return model;
    }

    private String nvl(String valor) {
        return valor != null ? valor : "";
    }
}