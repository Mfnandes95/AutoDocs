package com.example.demo.domain.service;

import com.example.demo.application.util.SanitizerUtils;
import com.example.demo.domain.dto.ImportResultDTO;
import com.example.demo.domain.model.Equipamento;
import com.example.demo.infrastructure.repository.EquipamentoRepository;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class EquipamentoImportService {

    // Colunas aceitas no cabeçalho (case-insensitive). "nome" é obrigatória;
    // as demais têm um valor padrão igual ao do cadastro manual (ver
    // EquipamentoController.criar).
    private static final String COL_NOME = "nome";
    private static final String COL_TIPO = "tipo";
    private static final String COL_PATRIMONIO = "patrimonio";
    private static final String COL_STATUS = "status";

    private final EquipamentoRepository equipamentoRepository;

    public EquipamentoImportService(EquipamentoRepository equipamentoRepository) {
        this.equipamentoRepository = equipamentoRepository;
    }

    public ImportResultDTO importar(MultipartFile arquivo) throws IOException {
        String nomeArquivo = arquivo.getOriginalFilename() != null
                ? arquivo.getOriginalFilename().toLowerCase(Locale.ROOT)
                : "";

        List<Map<String, String>> linhas;
        if (nomeArquivo.endsWith(".csv")) {
            linhas = lerCsv(arquivo);
        } else if (nomeArquivo.endsWith(".xlsx") || nomeArquivo.endsWith(".xls")) {
            linhas = lerXlsx(arquivo);
        } else {
            throw new IllegalArgumentException(
                    "Formato não suportado. Envie um arquivo .xlsx ou .csv.");
        }

        List<Equipamento> paraSalvar = new ArrayList<>();
        List<ImportResultDTO.LinhaComErro> erros = new ArrayList<>();

        int numeroLinha = 1; // linha 1 = cabeçalho
        for (Map<String, String> linha : linhas) {
            numeroLinha++;

            String nome = valor(linha, COL_NOME);
            if (nome.isBlank()) {
                erros.add(ImportResultDTO.LinhaComErro.builder()
                        .linha(numeroLinha)
                        .motivo("Coluna 'nome' vazia ou ausente — linha ignorada")
                        .build());
                continue;
            }

            String tipo = valor(linha, COL_TIPO);
            String status = valor(linha, COL_STATUS);
            String patrimonio = valor(linha, COL_PATRIMONIO);

            Equipamento eq = new Equipamento();
            eq.setNome(SanitizerUtils.sanitizar(nome));
            eq.setTipo(SanitizerUtils.sanitizar(tipo.isBlank() ? "Hardware" : tipo));
            eq.setStatus(SanitizerUtils.sanitizar(status.isBlank() ? "ATIVO" : status));
            eq.setPatrimonio(SanitizerUtils.sanitizar(patrimonio));

            paraSalvar.add(eq);
        }

        if (!paraSalvar.isEmpty()) {
            equipamentoRepository.saveAll(paraSalvar);
        }

        return ImportResultDTO.builder()
                .totalLinhas(linhas.size())
                .importados(paraSalvar.size())
                .ignorados(erros.size())
                .erros(erros)
                .build();
    }

    private String valor(Map<String, String> linha, String coluna) {
        String v = linha.get(coluna);
        return v == null ? "" : v.trim();
    }

    // ---------- XLSX ----------

    private List<Map<String, String>> lerXlsx(MultipartFile arquivo) throws IOException {
        List<Map<String, String>> resultado = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(arquivo.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() == 0) {
                return resultado;
            }

            Row cabecalho = sheet.getRow(sheet.getFirstRowNum());
            Map<Integer, String> colunasPorIndice = mapearCabecalho(cabecalho, formatter);

            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row linha = sheet.getRow(i);
                if (linha == null) continue;

                Map<String, String> mapaLinha = new HashMap<>();
                boolean linhaVazia = true;
                for (Map.Entry<Integer, String> col : colunasPorIndice.entrySet()) {
                    String valorCelula = formatter.formatCellValue(linha.getCell(col.getKey()));
                    if (!valorCelula.isBlank()) linhaVazia = false;
                    mapaLinha.put(col.getValue(), valorCelula);
                }
                if (!linhaVazia) {
                    resultado.add(mapaLinha);
                }
            }
        }
        return resultado;
    }

    private Map<Integer, String> mapearCabecalho(Row cabecalho, DataFormatter formatter) {
        Map<Integer, String> mapa = new HashMap<>();
        if (cabecalho == null) return mapa;
        for (int c = cabecalho.getFirstCellNum(); c < cabecalho.getLastCellNum(); c++) {
            String nomeColuna = formatter.formatCellValue(cabecalho.getCell(c))
                    .trim().toLowerCase(Locale.ROOT);
            if (!nomeColuna.isBlank()) {
                mapa.put(c, nomeColuna);
            }
        }
        return mapa;
    }

    // ---------- CSV ----------

    private List<Map<String, String>> lerCsv(MultipartFile arquivo) throws IOException {
        List<Map<String, String>> resultado = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8))) {

            String primeiraLinha = reader.readLine();
            if (primeiraLinha == null) return resultado;

            // Detecta o separador (',' ou ';') pelo cabeçalho — planilhas
            // exportadas em pt-BR normalmente usam ';'.
            char separador = primeiraLinha.chars().filter(ch -> ch == ';').count()
                    > primeiraLinha.chars().filter(ch -> ch == ',').count() ? ';' : ',';

            List<String> colunas = parseLinhaCsv(primeiraLinha, separador).stream()
                    .map(s -> s.trim().toLowerCase(Locale.ROOT))
                    .toList();

            String linhaTexto;
            while ((linhaTexto = reader.readLine()) != null) {
                if (linhaTexto.isBlank()) continue;

                List<String> valores = parseLinhaCsv(linhaTexto, separador);
                Map<String, String> mapaLinha = new HashMap<>();
                for (int i = 0; i < colunas.size() && i < valores.size(); i++) {
                    mapaLinha.put(colunas.get(i), valores.get(i).trim());
                }
                resultado.add(mapaLinha);
            }
        }
        return resultado;
    }

    /** Parser simples de uma linha CSV com suporte a valores entre aspas. */
    private List<String> parseLinhaCsv(String linha, char separador) {
        List<String> valores = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        boolean dentroDeAspas = false;

        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '"') {
                dentroDeAspas = !dentroDeAspas;
            } else if (c == separador && !dentroDeAspas) {
                valores.add(atual.toString());
                atual.setLength(0);
            } else {
                atual.append(c);
            }
        }
        valores.add(atual.toString());
        return valores;
    }
}
