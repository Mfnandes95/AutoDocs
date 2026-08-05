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

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/termos") // Ajustado para bater com o fetch do app.js
public class DocsController {

    // Logger estruturado para garantir trilha de auditoria (LGPD)
    private static final Logger logger = LoggerFactory.getLogger(DocsController.class);

    private final DocsService docsService;

    // Lista restrita de tipos MIME permitidos (Prevenção contra upload de arquivos maliciosos)
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "application/msword" // .doc
    );

    public DocsController(DocsService docsService) {
        this.docsService = docsService;
    }

    // =========================================================================================
    // 1. NOVO ENDPOINT DE GERAÇÃO (PDF) MESCLADO COM SUAS REGRAS DE SEGURANÇA E AUDITORIA
    // =========================================================================================
    @PostMapping(value = "/gerar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> gerarTermo(
            @RequestPart("file") MultipartFile file,
            @Valid @RequestPart("dto") TermoRequestDTO dto,
            Authentication authentication) {

        // Correção InfoSec: Validação do arquivo (Unrestricted File Upload)
        if (file == null || file.isEmpty() || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            logger.warn("Tentativa de upload de arquivo inválido/vazio pelo usuário: {}", authentication.getName());
            return ResponseEntity.badRequest().body(ApiResponse.erro("Arquivo inválido ou não suportado. Envie apenas documentos Word (.docx)."));
        }

        try {
            // Correção LGPD: Log de auditoria registrando quem gerou o documento
            logger.info("Usuário {} iniciou a geração de documento PDF.", authentication.getName());

            // Chama o service que agora retorna o PDF gerado
            byte[] pdfBytes = docsService.processarGeracao(file, dto);

            // Monta os cabeçalhos para o download do arquivo
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            
            ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename("Termo_Responsabilidade.pdf")
                    .build();
            headers.setContentDisposition(contentDisposition);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            // Captura erros de validação de negócio que você possa lançar no Service
            logger.warn("Erro de validação ao gerar PDF para o usuário {}: {}", authentication.getName(), e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.erro(e.getMessage()));
            
        } catch (Exception e) {
            // Correção InfoSec: Evita Information Disclosure ocultando a stack trace do cliente
            logger.error("Erro interno ao processar arquivo e gerar PDF para o usuário {}: ", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.erro("Ocorreu um erro interno ao processar sua solicitação. Tente novamente mais tarde."));
        }
    }

    // =========================================================================================
    // 2. MÉTODOS ORIGINAIS MANTIDOS INTACTOS (COM AS SUAS ROTAS E REGRAS DE NEGÓCIO)
    // =========================================================================================

    @GetMapping("/listar-todos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> listarTodos(Authentication authentication) {
        try {
            // Correção LGPD: Trilha de auditoria explícita para acesso em massa a dados sensíveis
            logger.info("Usuário {} (Roles: {}) realizou acesso à listagem completa de termos.", 
                        authentication.getName(), authentication.getAuthorities());

            List<DadosTermoDTO> lista = docsService.listarTodosOsTermos();
            return ResponseEntity.ok(ApiResponse.ok(lista));

        } catch (AccessDeniedException e) {
            logger.warn("Acesso negado para o usuário {} na listagem de termos.", authentication.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.erro("Acesso negado: permissão insuficiente para listar termos."));
        } catch (Exception e) {
            logger.error("Falha interna ao listar termos solicitada pelo usuário {}: ", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.erro("Falha ao recuperar a lista de termos."));
        }
    }

    @GetMapping("/estatisticas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> obterEstatisticas(Authentication authentication) {
        try {
            logger.debug("Usuário {} acessou as estatísticas.", authentication.getName());
            
            EstatisticasDTO estatisticas = docsService.obterEstatisticas();
            return ResponseEntity.ok(ApiResponse.ok(estatisticas));
            
        } catch (AccessDeniedException e) {
            logger.warn("Acesso negado para o usuário {} nas estatísticas.", authentication.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.erro("Acesso negado: permissão insuficiente para acessar estatísticas."));
        } catch (Exception e) {
            logger.error("Falha interna ao obter estatísticas para o usuário {}: ", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.erro("Falha ao obter estatísticas."));
        }
    }

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> obterDashboard(Authentication authentication) {
        try {
            logger.debug("Usuário {} acessou o dashboard.", authentication.getName());
            
            DashboardDTO dashboard = docsService.obterDashboard();
            return ResponseEntity.ok(ApiResponse.ok(dashboard));
            
        } catch (AccessDeniedException e) {
            logger.warn("Acesso negado para o usuário {} no dashboard.", authentication.getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.erro("Acesso negado: permissão insuficiente para acessar o dashboard."));
        } catch (Exception e) {
            logger.error("Falha interna ao obter dashboard para o usuário {}: ", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.erro("Falha ao carregar os dados do dashboard."));
        }
    }
}