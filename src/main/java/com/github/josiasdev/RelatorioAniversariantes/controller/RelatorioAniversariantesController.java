package com.github.josiasdev.RelatorioAniversariantes.controller;

import com.github.josiasdev.RelatorioAniversariantes.service.RelatorioAniversariantesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/relatorios")
@Tag(name = "Relatórios", description = "Endpoints para geração de relatórios automatizados")
public class RelatorioAniversariantesController {

    private final RelatorioAniversariantesService relatorioAniversariantesService;

    public RelatorioAniversariantesController(RelatorioAniversariantesService relatorioAniversariantesService) {
        this.relatorioAniversariantesService = relatorioAniversariantesService;
    }

    @GetMapping("/gerarAniversariantes")
    @Operation(
            summary = "Dispara a Geração do Relatório de Aniversariantes",
            description = "Inicia o processo de automação para extrair a lista de aniversariantes da semana. " +
                    "A operação é assíncrona e o servidor responde imediatamente."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "Requisição aceita. O relatório está sendo processado em segundo plano."
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Ocorreu um erro interno ao tentar iniciar o processo de geração do relatório."
            )
    })
    public ResponseEntity<String> gerarRelatorio() throws IOException, InterruptedException {
            relatorioAniversariantesService.gerarRelatorioAniversariantes();
            return ResponseEntity.accepted().body("Requisição recebida. O relatório está sendo gerado em segundo plano.");
    }
}
