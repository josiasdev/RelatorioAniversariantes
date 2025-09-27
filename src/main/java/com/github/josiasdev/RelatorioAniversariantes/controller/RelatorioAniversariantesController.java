package com.github.josiasdev.RelatorioAniversariantes.controller;

import com.github.josiasdev.RelatorioAniversariantes.service.RelatorioAniversariantesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relatorios")
public class RelatorioAniversariantesController {

    private final RelatorioAniversariantesService relatorioAniversariantesService;

    public RelatorioAniversariantesController(RelatorioAniversariantesService relatorioAniversariantesService) {
        this.relatorioAniversariantesService = relatorioAniversariantesService;
    }

    @GetMapping("/gerarAniversariantes")
    public ResponseEntity<String> gerarRelatorio(){
        try {
            relatorioAniversariantesService.gerarRelatorioAniversariantes();
            return ResponseEntity.accepted().body("Requisição recebida. O relatório está sendo gerado em segundo plano.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao iniciar a geração do relatório: " + e.getMessage());
        }
    }
}
