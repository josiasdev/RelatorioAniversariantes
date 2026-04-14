package com.github.josiasdev.RelatorioAniversariantes.controller;

import com.github.josiasdev.RelatorioAniversariantes.service.RelatorioAniversariantesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/relatorios")
@Tag(name = "Relatórios", description = "Endpoints para gestão e automação de relatórios")
public class RelatorioAniversariantesController {

    private final RelatorioAniversariantesService relatorioAniversariantesService;

    public RelatorioAniversariantesController(RelatorioAniversariantesService relatorioAniversariantesService) {
        this.relatorioAniversariantesService = relatorioAniversariantesService;
    }

    @GetMapping("/gerarAniversariantes")
    @Operation(
            summary = "Geração de Relatório Consolidado",
            description = "Dispara o processo automatizado que realiza o web scraping no sistema Church, " +
                    "processa os dados de Membros, Congregados e Casamentos, e gera um PDF unificado. " +
                    "O processamento é assíncrono."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Processamento iniciado com sucesso."),
            @ApiResponse(responseCode = "500", description = "Erro interno ao processar a solicitação.")
    })
    public ResponseEntity<Map<String, String>> gerarRelatorio() throws IOException, InterruptedException {
        log.info("Recebida requisição para gerar relatório unificado de aniversariantes.");

        relatorioAniversariantesService.gerarRelatorioAniversariantes();

        return ResponseEntity.accepted().body(Map.of(
                "status", "Processamento iniciado",
                "mensagem", "O robô está trabalhando. O arquivo PDF será gerado na raiz do projeto ao finalizar."
        ));
    }
}