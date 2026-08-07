package com.example.demo.application;

import com.example.demo.domain.model.InventarioEntity;
import com.example.demo.domain.service.InventarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    // GET /api/inventario - Lista todos os ativos
    @GetMapping
    public ResponseEntity<List<InventarioEntity>> listarInventario() {
        return ResponseEntity.ok(inventarioService.listarTodos());
    }

    // POST /api/inventario - Salva um novo ativo vindo do formulário
    @PostMapping
    public ResponseEntity<InventarioEntity> salvarItem(@RequestBody InventarioEntity item) {
        InventarioEntity salvo = inventarioService.salvar(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // POST /api/inventario/importar - Processa a planilha Excel
    @PostMapping("/importar")
    public ResponseEntity<Map<String, Object>> importarPlanilha(@RequestParam("file") MultipartFile file) {
        try {
            String mensagem = inventarioService.importarPlanilha(file);
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

    // DELETE /api/inventario/{id} - Remove um ativo pelo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarItem(@PathVariable Long id) {
        inventarioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}