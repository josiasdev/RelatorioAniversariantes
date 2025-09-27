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
            String nomeArquivo = relatorioAniversariantesService.gerarRelatorioAniversariantes();
            return ResponseEntity.ok("Relatório gerado com sucesso: " + nomeArquivo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar o relatório: " + e.getMessage());
        }
    }
}
