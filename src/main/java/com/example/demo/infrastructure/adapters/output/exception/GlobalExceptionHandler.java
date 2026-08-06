package com.example.demo.infrastructure.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Governança de TI / Observabilidade:
     * Trata arquivos estáticos não encontrados (CSS, JS, imagens) diretamente como HTTP 404,
     * impedindo a poluição dos logs de auditoria com erros internos (500).
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoResourceFoundException(NoResourceFoundException e) {
        logger.warn("Recurso estático não encontrado: {}", e.getResourcePath());

        Map<String, String> response = new HashMap<>();
        response.put("erro", "O recurso solicitado não foi encontrado no servidor.");
        
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Segurança da Informação (Information Disclosure):
     * Captura exceções genéricas/não tratadas sem expor o StackTrace para o cliente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception e) {
        logger.error("Erro não tratado capturado pelo GlobalExceptionHandler", e);

        Map<String, String> response = new HashMap<>();
        response.put("erro", "Ocorreu um problema no servidor. Tente novamente mais tarde.");

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}