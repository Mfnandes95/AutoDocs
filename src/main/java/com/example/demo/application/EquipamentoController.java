package com.example.demo.application;

import com.example.demo.application.util.SanitizerUtils;
import com.example.demo.domain.dto.ImportResultDTO;
import com.example.demo.domain.model.Equipamento;
import com.example.demo.domain.response.ApiResponse;
import com.example.demo.domain.service.EquipamentoImportService;
import com.example.demo.infrastructure.repository.EquipamentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/equipamentos")
public class EquipamentoController {
    // Correção InfoSec: @CrossOrigin("*") removido — a origem já é controlada
    // centralmente pelo CorsConfigurationSource em SecurityConfig.

    private static final Logger logger = LoggerFactory.getLogger(EquipamentoController.class);
    private static final long TAMANHO_MAXIMO_BYTES = 5 * 1024 * 1024; // 5 MB

    private final EquipamentoRepository equipamentoRepository;
    private final EquipamentoImportService equipamentoImportService;

    public EquipamentoController(EquipamentoRepository equipamentoRepository,
                                  EquipamentoImportService equipamentoImportService) {
        this.equipamentoRepository = equipamentoRepository;
        this.equipamentoImportService = equipamentoImportService;
    }

    @GetMapping
    public List<Equipamento> listar() {
        return equipamentoRepository.findAll();
    }

    @GetMapping("/buscar")
    public List<Equipamento> buscar(@RequestParam String q) {
        return equipamentoRepository.findByNomeContainingIgnoreCase(q);
    }

    @PostMapping
    public ResponseEntity<Equipamento> criar(@RequestBody Equipamento equipamento) {
        if (equipamento.getNome() == null || equipamento.getNome().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (equipamento.getTipo() == null || equipamento.getTipo().isBlank()) {
            equipamento.setTipo("Hardware");
        }
        if (equipamento.getStatus() == null || equipamento.getStatus().isBlank()) {
            equipamento.setStatus("ATIVO");
        }

        // Correção InfoSec (XSS armazenado): sanitiza todo campo de texto livre
        // antes de persistir, já que o frontend injeta esses valores via innerHTML.
        equipamento.setNome(SanitizerUtils.sanitizar(equipamento.getNome()));
        equipamento.setTipo(SanitizerUtils.sanitizar(equipamento.getTipo()));
        equipamento.setStatus(SanitizerUtils.sanitizar(equipamento.getStatus()));
        equipamento.setPatrimonio(SanitizerUtils.sanitizar(equipamento.getPatrimonio()));

        return ResponseEntity.ok(equipamentoRepository.save(equipamento));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Equipamento> atualizar(
            @PathVariable Long id,
            @RequestBody Equipamento dados) {

        return equipamentoRepository.findById(id)
                .map(eq -> {
                    if (dados.getNome()        != null && !dados.getNome().isBlank())        eq.setNome(SanitizerUtils.sanitizar(dados.getNome()));
                    if (dados.getTipo()        != null && !dados.getTipo().isBlank())        eq.setTipo(SanitizerUtils.sanitizar(dados.getTipo()));
                    if (dados.getStatus()      != null && !dados.getStatus().isBlank())      eq.setStatus(SanitizerUtils.sanitizar(dados.getStatus()));
                    if (dados.getPatrimonio()  != null && !dados.getPatrimonio().isBlank())  eq.setPatrimonio(SanitizerUtils.sanitizar(dados.getPatrimonio()));
                    return ResponseEntity.ok(equipamentoRepository.save(eq));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/importar")
    public ResponseEntity<ApiResponse<?>> importar(@RequestParam("arquivo") MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.erro("Arquivo vazio."));
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            return ResponseEntity.badRequest().body(ApiResponse.erro("Arquivo maior que 5 MB."));
        }

        try {
            ImportResultDTO resultado = equipamentoImportService.importar(arquivo);
            logger.info("Importação de inventário: {} de {} linhas importadas ({} ignoradas)",
                    resultado.getImportados(), resultado.getTotalLinhas(), resultado.getIgnorados());
            return ResponseEntity.ok(ApiResponse.ok(resultado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.erro(e.getMessage()));
        } catch (IOException e) {
            logger.warn("Falha ao ler arquivo de importação", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.erro("Não foi possível ler o arquivo. Confira o formato (.xlsx ou .csv)."));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!equipamentoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        equipamentoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<?>> tratarArquivoGrande(MaxUploadSizeExceededException e) {
        return ResponseEntity.badRequest().body(ApiResponse.erro("Arquivo maior que 5 MB."));
    }
}