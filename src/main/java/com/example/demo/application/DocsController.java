package com.example.demo.application;

import com.example.demo.domain.dto.DadosTermoDTO;
import com.example.demo.domain.dto.DashboardDTO;
import com.example.demo.domain.dto.EstatisticasDTO;
import com.example.demo.domain.dto.TermoRequestDTO;
import com.example.demo.domain.response.ApiResponse;
import com.example.demo.domain.service.DocsService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/termos")
public class DocsController {

    private static final Logger logger = LoggerFactory.getLogger(DocsController.class);

    private static final MediaType MEDIA_TYPE_DOCX = MediaType.valueOf(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    private final DocsService docsService;

    public DocsController(DocsService docsService) {
        this.docsService = docsService;
    }

    @PostMapping(value = "/gerar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> gerarTermo(
            @RequestPart("file") MultipartFile file,
            @Valid @RequestPart("dto") TermoRequestDTO dto,
            Authentication authentication) {

        String userName = (authentication != null) ? authentication.getName() : "Usuário Anônimo";

        // Sanitização e validação resiliente do arquivo
        if (file == null || file.isEmpty()) {
            logger.warn("Upload de arquivo vazio ou nulo recebido de: {}", userName);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.erro("Selecione um arquivo de modelo (.docx) válido."));
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".docx")) {
            logger.warn("Formato de arquivo inválido enviado por {}: {}", userName, originalFilename);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.erro("Formato não suportado. Envie apenas modelos do Word (.docx)."));
        }

        try {
            logger.info("Iniciando geração de Termo de Responsabilidade para usuário: {}", userName);

            byte[] docxBytes = docsService.processarGeracao(file, dto);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MEDIA_TYPE_DOCX);
            headers.setContentDisposition(
                    ContentDisposition.builder("attachment")
                            .filename("Termo_Responsabilidade.docx")
                            .build()
            );

            return new ResponseEntity<>(docxBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            logger.warn("Falha de validação nas regras de negócio para {}: {}", userName, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.erro(e.getMessage()));

        } catch (Exception e) {
            logger.error("Erro crítico ao gerar termo para {}: ", userName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.erro("Erro ao processar o documento Word. Verifique a estrutura do modelo enviado."));
        }
    }

    @GetMapping("/listar-todos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> listarTodos(Authentication authentication) {
        try {
            List<DadosTermoDTO> lista = docsService.listarTodosOsTermos();
            return ResponseEntity.ok(ApiResponse.ok(lista));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.erro("Acesso negado."));
        } catch (Exception e) {
            logger.error("Erro ao listar termos: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.erro("Falha ao recuperar a lista de termos."));
        }
    }

    @GetMapping("/estatisticas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> obterEstatisticas(Authentication authentication) {
        try {
            EstatisticasDTO estatisticas = docsService.obterEstatisticas();
            return ResponseEntity.ok(ApiResponse.ok(estatisticas));
        } catch (Exception e) {
            logger.error("Erro ao obter estatísticas: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.erro("Falha ao obter estatísticas."));
        }
    }

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> obterDashboard(Authentication authentication) {
        try {
            DashboardDTO dashboard = docsService.obterDashboard();
            return ResponseEntity.ok(ApiResponse.ok(dashboard));
        } catch (Exception e) {
            logger.error("Erro ao carregar dashboard: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.erro("Falha ao carregar os dados do dashboard."));
        }
    }
}