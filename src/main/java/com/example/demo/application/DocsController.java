package com.example.demo.application;

import com.example.demo.domain.dto.EquipamentoDTO;
import com.example.demo.domain.dto.EscritoriosDTO;
import com.example.demo.domain.dto.ResponsavelDTO;
import com.example.demo.domain.dto.UnidadeDTO;
import com.example.demo.domain.model.TermoEntity;
import com.example.demo.infrastructure.repository.TermoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/docs")
@CrossOrigin(origins = "*")
public class DocsController {

    private final TermoRepository termoRepository;

    public DocsController(TermoRepository termoRepository) {
        this.termoRepository = termoRepository;
    }

    @PostMapping(value = "/gerar-dinamico", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('TECNICO', 'GESTOR')")
    public ResponseEntity<byte[]> gerarDinamico(
            @RequestPart("file") MultipartFile file,
            @RequestPart("dados") String dadosJson) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode dados = mapper.readTree(dadosJson);

            String nomeColaborador = dados.has("nome_colaborador") ? dados.get("nome_colaborador").asText() : 
                                    dados.has("nomeColaborador") ? dados.get("nomeColaborador").asText() : "colaborador";
            
            String nomeArquivo = "Termo_" + nomeColaborador.replaceAll("\\s+", "_") + ".docx";

            TermoEntity termo = new TermoEntity();
            termo.setNomeColaborador(nomeColaborador);
            termo.setPatrimonio(dados.has("patrimonio") ? dados.get("patrimonio").asText() : null);
            termo.setUnidade(dados.has("unidade") ? dados.get("unidade").asText() : null);
            termo.setInfo(dados.has("info") ? dados.get("info").asText() : null);
            termo.setTipo(dados.has("tipo") ? dados.get("tipo").asText() : null);
            termo.setStatusAparelho("Ativo");

            String dataInicioStr = dados.has("data_inicio") ? dados.get("data_inicio").asText() : null;
            if (dataInicioStr != null && !dataInicioStr.isEmpty()) {
                termo.setDataInicio(dataInicioStr.length() == 10 ? LocalDate.parse(dataInicioStr).atStartOfDay() : LocalDateTime.parse(dataInicioStr));
            }
            String dataTerminoStr = dados.has("data_termino") ? dados.get("data_termino").asText() : null;
            if (dataTerminoStr != null && !dataTerminoStr.isEmpty()) {
                termo.setDataTermino(dataTerminoStr.length() == 10
                    ? LocalDate.parse(dataTerminoStr).atStartOfDay()
                    : LocalDateTime.parse(dataTerminoStr));
            }

            termoRepository.save(termo);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(file.getBytes());

        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/listar-todos")
    public ResponseEntity<List<TermoEntity>> listarTodos() {
        return ResponseEntity.ok(termoRepository.findAll());
    }

    @GetMapping("/estatisticas")
    @PreAuthorize("anyHasRole('TECNICO', 'GESTOR')")
    public ResponseEntity<Map<String, Long>> getEstatisticas() {
        List<TermoEntity> todos = termoRepository.findAll();
        Map<String, Long> stats = new HashMap<>();

        stats.put("Macapá", todos.stream().filter(t -> "Macapá".equals(t.getUnidade())).count());
        stats.put("Santana", todos.stream().filter(t -> "Escritório de Santana".equals(t.getUnidade())).count());
        stats.put("Oiapoque", todos.stream().filter(t -> "Escritório de Oiapoque".equals(t.getUnidade())).count());
        stats.put("Laranjal do Jari", todos.stream().filter(t -> "Escritório de Laranjal do Jari".equals(t.getUnidade())).count());
        stats.put("Porto Grande", todos.stream().filter(t -> "Escritório de Porto Grande".equals(t.getUnidade())).count());
        stats.put("Tartarugalzinho", todos.stream().filter(t -> "Escritório de Tartarugalzinho".equals(t.getUnidade())).count());
        
        stats.put("Total", (long) todos.size());
        stats.put("Avarias", todos.stream().filter(t -> "AVARIADO".equals(t.getStatusAparelho())).count());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("anyHasRole('TECNICO', 'GESTOR')")
    public ResponseEntity<List<EscritoriosDTO>> getDashboard() {
        List<TermoEntity> todos = termoRepository.findAll(); 
        Map<String, Map<String, Map<String, List<TermoEntity>>>> estrutura = new HashMap<>();

        for (TermoEntity termo : todos) { 
            String predio = termo.getUnidade() != null ? termo.getUnidade() : "Sem prédio";
            String unidade = "Setor Geral"; 
            String responsavel = termo.getNomeColaborador() != null ? termo.getNomeColaborador() : "Sem responsável";
        
            estrutura.computeIfAbsent(predio, k -> new HashMap<>()) 
                    .computeIfAbsent(unidade, k -> new HashMap<>())
                    .computeIfAbsent(responsavel, k -> new ArrayList<>())
                    .add(termo);
        }

        List<EscritoriosDTO> resultado = new ArrayList<>();

        for (var predioEntry : estrutura.entrySet()) {
            EscritoriosDTO escritorioDTO = new EscritoriosDTO();
            escritorioDTO.setNome(predioEntry.getKey()); // FIX: Trocado setNome para setNomePredio
            
            long totalEquipamentos = 0;
            long totalAvarias = 0;
            List<UnidadeDTO> unidadesDTO = new ArrayList<>();

            for (var unidadeEntry : predioEntry.getValue().entrySet()) {
                UnidadeDTO unidadeDTO = new UnidadeDTO();
                unidadeDTO.setNome(unidadeEntry.getKey());
                List<ResponsavelDTO> responsaveisDTO = new ArrayList<>();

                for (var responsavelEntry : unidadeEntry.getValue().entrySet()) {
                    ResponsavelDTO responsavelDTO = new ResponsavelDTO();
                    responsavelDTO.setNome(responsavelEntry.getKey());
                    
                    List<EquipamentoDTO> equipamentosDTO = responsavelEntry.getValue().stream()
                        .map(t -> {
                            EquipamentoDTO eqDTO = new EquipamentoDTO();
                            eqDTO.setPatrimonio(t.getPatrimonio() != null ? t.getPatrimonio() : "Sem patrimônio");
                            eqDTO.setNome(t.getEquipamento() != null ? t.getEquipamento() : "Sem equipamento");
                            eqDTO.setDescricao(t.getDescricao() != null ? t.getDescricao() : "Sem descrição");
                            eqDTO.setStatusAparelho(t.getStatusAparelho());
                            return eqDTO;
                        }).collect(Collectors.toList());

                    responsavelDTO.setEquipamentos(equipamentosDTO);
                    totalEquipamentos += equipamentosDTO.size();
                    totalAvarias += equipamentosDTO.stream()
                        .filter(e -> "AVARIADO".equalsIgnoreCase(e.getStatusAparelho()))
                        .count();

                    responsaveisDTO.add(responsavelDTO);
                }
                unidadeDTO.setResponsaveis(responsaveisDTO);
                unidadesDTO.add(unidadeDTO);
            }
            
            escritorioDTO.setUnidades(unidadesDTO);
            escritorioDTO.setTotalTermos(totalEquipamentos);
            escritorioDTO.setTotalAvarias(totalAvarias);
            resultado.add(escritorioDTO);
        }
        
        return ResponseEntity.ok(resultado);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('TECNICO', 'GESTOR')")
    public ResponseEntity<Void> atualizarStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        if(!termoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        TermoEntity termo = termoRepository.findById(id).orElseThrow();
        termo.setStatusAparelho(status);
        termoRepository.save(termo);

        return ResponseEntity.ok().build();
    }
}