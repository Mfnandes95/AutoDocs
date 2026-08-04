package com.example.demo.application;

import com.example.demo.domain.dto.DadosTermoDTO;
import com.example.demo.domain.dto.DashboardDTO;
import com.example.demo.domain.dto.EstatisticasDTO;
import com.example.demo.domain.dto.TermoRequestDTO;
import com.example.demo.domain.response.ApiResponse;
import com.example.demo.domain.service.DocsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/docs")
public class DocsController {

    // Logger estruturado para substituir o e.printStackTrace() e garantir trilha de auditoria (LGPD)
    private static final Logger logger = LoggerFactory.getLogger(DocsController.class);

    private final DocsService docsService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    // Lista restrita de tipos MIME permitidos (Prevenção contra upload de arquivos maliciosos)
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "application/msword" // .doc
    );

    public DocsController(DocsService docsService, ObjectMapper objectMapper, Validator validator) {
        this.docsService = docsService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping(value = "/gerar-dinamico", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> gerarDinamico(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "dados", required = false) String dadosJson,
            Authentication authentication) {

        if (dadosJson == null || dadosJson.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.erro("Dados do formulário ausentes."));
        }

        // Correção InfoSec: Validação do arquivo (Unrestricted File Upload)
        if (file == null || file.isEmpty() || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            logger.warn("Tentativa de upload de arquivo inválido/vazio pelo usuário: {}", authentication.getName());
            return ResponseEntity.badRequest().body(ApiResponse.erro("Arquivo inválido ou não suportado. Envie apenas documentos Word."));
        }

        try {
            TermoRequestDTO dados = objectMapper.readValue(dadosJson, TermoRequestDTO.class);

            // Validação de negócio no DTO (Garante que dados sensíveis/obrigatórios não cheguem corrompidos no back-end)
            var violacoes = validator.validate(dados);
            if (!violacoes.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.erro("Os dados fornecidos no formulário são inválidos."));
            }

            // Correção LGPD: Log de auditoria registrando quem gerou o documento
            logger.info("Usuário {} iniciou a geração de documento dinâmico.", authentication.getName());

            byte[] documento = docsService.processarGeracao(file, dados);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Termo_Gerado.docx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .body(documento);

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            logger.warn("Falha no parse do JSON enviado pelo usuário {}: {}", authentication.getName(), e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.erro("Formato de dados JSON inválido."));
        } catch (Exception e) {
            // Correção InfoSec: Evita Information Disclosure ocultando a stack trace do cliente
            logger.error("Erro interno ao processar arquivo para o usuário {}: ", authentication.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.erro("Ocorreu um erro interno ao processar sua solicitação. Tente novamente mais tarde."));
        }
    }

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