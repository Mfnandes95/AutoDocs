package com.example.demo.domain.service;

import com.example.demo.domain.model.ColaboradorEntity;
import com.example.demo.infrastructure.repository.ColaboradorRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ColaboradorService {

    private final ColaboradorRepository repository;

    public ColaboradorService(ColaboradorRepository repository) {
        this.repository = repository;
    }

    public List<ColaboradorEntity> listarTodos() {
        return repository.findAll();
    }

    public List<ColaboradorEntity> buscar(String q) {
        return repository.findByNomeContainingIgnoreCase(q == null ? "" : q);
    }

    @Transactional
    public String importarPlanilha(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo de planilha está vazio ou não foi fornecido.");
        }

        String nomeArquivo = file.getOriginalFilename() != null
                ? file.getOriginalFilename().toLowerCase(Locale.ROOT)
                : "";

        List<LinhaColaborador> linhas;
        try {
            if (nomeArquivo.endsWith(".csv")) {
                linhas = lerCsv(file);
            } else if (nomeArquivo.endsWith(".xlsx") || nomeArquivo.endsWith(".xls")) {
                linhas = lerXlsx(file);
            } else {
                throw new IllegalArgumentException("Formato não suportado. Envie um arquivo .xlsx, .xls ou .csv.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível ler o arquivo: " + e.getMessage(), e);
        }

        // Carrega os colaboradores já cadastrados num Map em memória (evita N
        // queries e resolve duplicidade dentro da própria planilha) — mesmo
        // truque usado no InventarioService.
        Map<String, ColaboradorEntity> cachePorNome = repository.findAll().stream()
                .collect(Collectors.toMap(
                        c -> normalizeText(c.getNome()),
                        c -> c,
                        (existente, duplicado) -> existente));

        int novos = 0;
        int atualizados = 0;

        for (LinhaColaborador linha : linhas) {
            String nome = sanitizeText(linha.nome);
            if (nome.isBlank()) continue;

            String chave = normalizeText(nome);
            boolean jaExiste = cachePorNome.containsKey(chave);

            ColaboradorEntity colaborador = cachePorNome.computeIfAbsent(chave, k -> new ColaboradorEntity());
            colaborador.setNome(nome);
            colaborador.setMatricula(sanitizeText(linha.matricula));
            colaborador.setSetor(sanitizeText(linha.setor));

            repository.save(colaborador);

            if (jaExiste) atualizados++; else novos++;
        }

        return String.format("Importação concluída! %d colaborador(es) novo(s) e %d atualizado(s).", novos, atualizados);
    }

    // ---------- XLSX / XLS ----------

    private List<LinhaColaborador> lerXlsx(MultipartFile file) throws IOException {
        List<LinhaColaborador> resultado = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                return resultado;
            }

            Row cabecalho = sheet.getRow(sheet.getFirstRowNum());
            int colNome = -1, colMatricula = -1, colSetor = -1;

            if (cabecalho != null) {
                for (Cell cell : cabecalho) {
                    String header = normalizeText(formatter.formatCellValue(cell));
                    if (header.contains("nome") || header.contains("colaborador") || header.contains("funcionario")) {
                        colNome = cell.getColumnIndex();
                    } else if (header.contains("matricula") || header.contains("registro") || header.contains("id")) {
                        colMatricula = cell.getColumnIndex();
                    } else if (header.contains("setor") || header.contains("unidade") || header.contains("departamento") || header.contains("cargo")) {
                        colSetor = cell.getColumnIndex();
                    }
                }
            }
            if (colNome == -1) colNome = 0; // fallback: primeira coluna é o nome

            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String nome = formatter.formatCellValue(row.getCell(colNome));
                if (nome == null || nome.isBlank()) continue;

                String matricula = colMatricula >= 0 ? formatter.formatCellValue(row.getCell(colMatricula)) : "";
                String setor = colSetor >= 0 ? formatter.formatCellValue(row.getCell(colSetor)) : "";
                resultado.add(new LinhaColaborador(nome, matricula, setor));
            }
        }
        return resultado;
    }

    // ---------- CSV ----------

    private List<LinhaColaborador> lerCsv(MultipartFile file) throws IOException {
        List<LinhaColaborador> resultado = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String primeiraLinha = reader.readLine();
            if (primeiraLinha == null) return resultado;

            char separador = primeiraLinha.chars().filter(ch -> ch == ';').count()
                    > primeiraLinha.chars().filter(ch -> ch == ',').count() ? ';' : ',';

            List<String> colunas = parseLinhaCsv(primeiraLinha, separador).stream()
                    .map(s -> normalizeText(s))
                    .toList();

            int colNome = -1, colMatricula = -1, colSetor = -1;
            for (int i = 0; i < colunas.size(); i++) {
                String c = colunas.get(i);
                if (c.contains("nome") || c.contains("colaborador") || c.contains("funcionario")) colNome = i;
                else if (c.contains("matricula") || c.contains("registro") || c.contains("id")) colMatricula = i;
                else if (c.contains("setor") || c.contains("unidade") || c.contains("departamento") || c.contains("cargo")) colSetor = i;
            }
            if (colNome == -1) colNome = 0;

            String linhaTexto;
            while ((linhaTexto = reader.readLine()) != null) {
                if (linhaTexto.isBlank()) continue;
                List<String> valores = parseLinhaCsv(linhaTexto, separador);

                String nome = colNome < valores.size() ? valores.get(colNome) : "";
                if (nome.isBlank()) continue;

                String matricula = (colMatricula >= 0 && colMatricula < valores.size()) ? valores.get(colMatricula) : "";
                String setor = (colSetor >= 0 && colSetor < valores.size()) ? valores.get(colSetor) : "";
                resultado.add(new LinhaColaborador(nome, matricula, setor));
            }
        }
        return resultado;
    }

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

    // ---------- Sanitização / normalização ----------

    private String sanitizeText(String input) {
        if (input == null) return "";
        return input.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
    }

    private String normalizeText(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private record LinhaColaborador(String nome, String matricula, String setor) {}
}
