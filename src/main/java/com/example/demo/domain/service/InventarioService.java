package com.example.demo.domain.service;

import com.example.demo.domain.model.InventarioEntity;
import com.example.demo.infrastructure.repository.InventarioRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventarioService {

    private final InventarioRepository repository;

    public InventarioService(InventarioRepository repository) {
        this.repository = repository;
    }

    public List<InventarioEntity> listarTodos() {
        return repository.findAll();
    }

    @Transactional
    public InventarioEntity salvar(InventarioEntity ativo) {
        if (ativo == null) {
            throw new IllegalArgumentException("O objeto de inventário não pode ser nulo.");
        }

        // Sanitização dos dados inseridos
        String patrimonioSanitizado = sanitizeText(ativo.getPatrimonio());
        String nomeSanitizado = sanitizeText(ativo.getNome());

        if (patrimonioSanitizado.isBlank() || nomeSanitizado.isBlank()) {
            throw new IllegalArgumentException("Patrimônio e Nome do equipamento são campos obrigatórios.");
        }

        ativo.setPatrimonio(patrimonioSanitizado);
        ativo.setNome(nomeSanitizado);
        ativo.setLocalizacao(sanitizeWithDefault(ativo.getLocalizacao(), "Macapá"));
        ativo.setStatus(sanitizeWithDefault(ativo.getStatus(), "Ativo"));

        // Se for uma inserção sem ID, mas o patrimônio já existir, faz a vinculação ao registro existente
        if (ativo.getId() == null) {
            repository.findByPatrimonio(patrimonioSanitizado)
                    .ifPresent(existente -> ativo.setId(existente.getId()));
        }

        return repository.save(ativo);
    }

    @Transactional
    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Ativo não encontrado para o ID fornecido: " + id);
        }
        repository.deleteById(id);
    }

    @Transactional
    public String importarPlanilha(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo de planilha está vazio ou não foi fornecido.");
        }

        DataFormatter formatter = new DataFormatter();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new IllegalArgumentException("A planilha está vazia ou não possui dados na primeira aba.");
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new IllegalArgumentException("A planilha não possui um cabeçalho válido na linha 1.");
            }

            // 1. Mapeamento dinâmico dos índices de colunas
            int colPatrimonio = -1;
            int colNome = -1;
            int colLocalizacao = -1;
            int colStatus = -1;

            for (Cell cell : headerRow) {
                String header = normalizeText(formatter.formatCellValue(cell));

                if (header.contains("patrimonio") || header.contains("tag") || header.contains("codigo")) {
                    colPatrimonio = cell.getColumnIndex();
                } else if (header.contains("equipamento") || header.contains("nome") || header.contains("descricao") || header.contains("item")) {
                    colNome = cell.getColumnIndex();
                } else if (header.contains("localizacao") || header.contains("unidade") || header.contains("setor")) {
                    colLocalizacao = cell.getColumnIndex();
                } else if (header.contains("status") || header.contains("situacao")) {
                    colStatus = cell.getColumnIndex();
                }
            }

            // Fallbacks de posição padrão caso o cabeçalho não seja identificado
            if (colPatrimonio == -1) colPatrimonio = 0;
            if (colNome == -1) colNome = 1;
            if (colLocalizacao == -1) colLocalizacao = 2;
            if (colStatus == -1) colStatus = 3;

            // 2. Carrega todos os registros atuais em um Map em memória (O(1) de busca)
            // Isso evita N queries no banco e previne duplicações na própria planilha
            Map<String, InventarioEntity> cachePatrimonio = repository.findAll().stream()
                    .collect(Collectors.toMap(
                            InventarioEntity::getPatrimonio,
                            entidade -> entidade,
                            (existente, duplicado) -> existente
                    ));

            int novosInseridos = 0;
            int atualizados = 0;

            // 3. Processamento, sanitização e Upsert resiliente dos itens
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String patrimonio = sanitizeText(formatter.formatCellValue(row.getCell(colPatrimonio)));
                String nome = sanitizeText(formatter.formatCellValue(row.getCell(colNome)));
                String localizacao = sanitizeWithDefault(formatter.formatCellValue(row.getCell(colLocalizacao)), "Macapá");
                String status = sanitizeWithDefault(formatter.formatCellValue(row.getCell(colStatus)), "Ativo");

                // Processa apenas se houver identificação mínima (Patrimônio e Nome)
                if (!patrimonio.isBlank() && !nome.isBlank()) {
                    boolean jaExiste = cachePatrimonio.containsKey(patrimonio);

                    // Recupera do cache ou instancia uma nova entidade já registrada no cache
                    InventarioEntity inventario = cachePatrimonio.computeIfAbsent(patrimonio, p -> {
                        InventarioEntity novo = new InventarioEntity();
                        novo.setPatrimonio(p);
                        return novo;
                    });

                    inventario.setNome(nome);
                    inventario.setLocalizacao(localizacao);
                    inventario.setStatus(status);

                    repository.save(inventario);

                    if (jaExiste) {
                        atualizados++;
                    } else {
                        novosInseridos++;
                    }
                }
            }

            return String.format("Importação concluída! %d novos ativos cadastrados e %d atualizados.", novosInseridos, atualizados);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar a planilha: " + e.getMessage(), e);
        }
    }

    // --- MÉTODOS AUXILIARES DE SANITIZAÇÃO E NORMALIZAÇÃO ---

    /**
     * Remove caracteres não imprimíveis de controle ASCII/Unicode e espaços em excesso.
     */
    private String sanitizeText(String input) {
        if (input == null) return "";
        return input.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "").trim();
    }

    /**
     * Sanitiza o texto e aplica um valor padrão se o resultado for vazio/em branco.
     */
    private String sanitizeWithDefault(String input, String defaultValue) {
        String sanitized = sanitizeText(input);
        return sanitized.isBlank() ? defaultValue : sanitized;
    }

    /**
     * Remove acentuação e converte para caixa baixa para mapeamento dinâmico de cabeçalhos.
     */
    private String normalizeText(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }
}