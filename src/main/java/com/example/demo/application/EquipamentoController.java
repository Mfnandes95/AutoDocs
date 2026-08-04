package com.example.demo.application;

import com.example.demo.domain.model.Equipamento;
import com.example.demo.infrastructure.repository.EquipamentoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/equipamentos")
@CrossOrigin(origins = "*")
public class EquipamentoController {

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
        return ResponseEntity.ok(equipamentoRepository.save(equipamento));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Equipamento> atualizar(
            @PathVariable Long id,
            @RequestBody Equipamento dados) {

        return equipamentoRepository.findById(id)
                .map(eq -> {
                    if (dados.getNome()        != null && !dados.getNome().isBlank())        eq.setNome(dados.getNome());
                    if (dados.getTipo()        != null && !dados.getTipo().isBlank())        eq.setTipo(dados.getTipo());
                    if (dados.getStatus()      != null && !dados.getStatus().isBlank())      eq.setStatus(dados.getStatus());
                    if (dados.getPatrimonio()  != null && !dados.getPatrimonio().isBlank())  eq.setPatrimonio(dados.getPatrimonio());
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