package com.example.demo.infrastructure.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Captura qualquer erro inesperado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception e) {
        // Correção InfoSec (Information Disclosure): a mensagem/stack trace da
        // exceção nunca vai para o cliente — fica só no log do servidor.
        logger.error("Erro não tratado capturado pelo GlobalExceptionHandler", e);

        Map<String, String> response = new HashMap<>();
        response.put("erro", "Ocorreu um problema no servidor. Tente novamente mais tarde.");

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}