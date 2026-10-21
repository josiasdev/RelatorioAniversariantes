package com.github.josiasdev.RelatorioAniversariantes.exception.handler;

import com.github.josiasdev.RelatorioAniversariantes.exception.RelatorioEmAndamentoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RelatorioEmAndamentoException.class)
    public ResponseEntity<Map<String, Object>> handleRelatorioEmAndamento(RelatorioEmAndamentoException e) {
        log.warn("Conflito de concorrência na geração de relatório: {}", e.getMessage());
        return montarResposta(HttpStatus.CONFLICT, "Conflito", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Requisição inválida: {}", e.getMessage());
        return montarResposta(HttpStatus.BAD_REQUEST, "Requisição inválida", e.getMessage());
    }

    private ResponseEntity<Map<String, Object>> montarResposta(HttpStatus status, String erro, String mensagem) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "erro", erro,
                "mensagem", mensagem == null ? "" : mensagem
        ));
    }
}
