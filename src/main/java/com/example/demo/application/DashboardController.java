package com.example.demo.application;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    // Se você tiver um serviço para buscar os dados, injete-o aqui
    // private final DashboardService dashboardService;

    @GetMapping("/relatorio")
    public ResponseEntity<Map<String, Object>> getRelatorio() {
        // Exemplo de retorno de dados (substitua pela lógica real do seu sistema)
        Map<String, Object> dados = new HashMap<>();
        dados.put("totalItens", 150);
        dados.put("ativos", 120);
        dados.put("manutencao", 30);
        
        return ResponseEntity.ok(dados);
    }
}