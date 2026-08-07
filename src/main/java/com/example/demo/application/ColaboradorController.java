package com.example.demo.application;

import com.example.demo.domain.model.ColaboradorEntity;
import com.example.demo.domain.service.ColaboradorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/colaboradores")
public class ColaboradorController {

    private final ColaboradorService colaboradorService;

    public ColaboradorController(ColaboradorService colaboradorService) {
        this.colaboradorService = colaboradorService;
    }

    // GET /api/colaboradores - lista completa, usada para popular o
    // autocomplete no formulário de geração de termo.
    @GetMapping
    public ResponseEntity<List<ColaboradorEntity>> listar() {
        return ResponseEntity.ok(colaboradorService.listarTodos());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ColaboradorEntity>> buscar(@RequestParam String q) {
        return ResponseEntity.ok(colaboradorService.buscar(q));
    }

    // POST /api/colaboradores/importar - planilha (.xlsx/.xls/.csv) com
    // colunas nome (obrigatória), matricula e setor (opcionais).
    @PostMapping("/importar")
    public ResponseEntity<Map<String, Object>> importarPlanilha(@RequestParam("file") MultipartFile file) {
        try {
            String mensagem = colaboradorService.importarPlanilha(file);
            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", mensagem
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "sucesso", false,
                    "mensagem", "Erro na importação: " + e.getMessage()
            ));
        }
    }
}
