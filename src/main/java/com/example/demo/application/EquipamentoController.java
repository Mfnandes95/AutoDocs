package com.example.demo.application;

import com.example.demo.application.util.SanitizerUtils;
import com.example.demo.domain.model.Equipamento;
import com.example.demo.infrastructure.repository.EquipamentoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/equipamentos")
public class EquipamentoController {
    // Correção InfoSec: @CrossOrigin("*") removido — a origem já é controlada
    // centralmente pelo CorsConfigurationSource em SecurityConfig.

    private final EquipamentoRepository equipamentoRepository;

    public EquipamentoController(EquipamentoRepository equipamentoRepository) {
        this.equipamentoRepository = equipamentoRepository;
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!equipamentoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        equipamentoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}