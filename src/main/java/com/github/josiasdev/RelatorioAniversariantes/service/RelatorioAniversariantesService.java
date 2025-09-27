package com.github.josiasdev.RelatorioAniversariantes.service;

import com.github.josiasdev.RelatorioAniversariantes.dto.AniversarianteDTO;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class RelatorioAniversariantesService {
    private final WebScraperService webScraperService;
    private final ExcelService excelService;

    public RelatorioAniversariantesService(WebScraperService webScraperService, ExcelService excelService) {
        this.webScraperService = webScraperService;
        this.excelService = excelService;
    }

    @Async
    public void gerarRelatorioAniversariantes() throws IOException, InterruptedException {
        LocalDate today =  LocalDate.now();
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        System.out.println("Iniciando processo de geração de relatório para a semana de " + startOfWeek + " a " + endOfWeek);

        List<AniversarianteDTO> aniversariantes = webScraperService.extrairAniversariantesDaSemana(startOfWeek, endOfWeek);

        if (aniversariantes == null || aniversariantes.isEmpty()) {
            System.out.println("Nenhum aniversariante encontrado para o período.");
            return;
        }
        System.out.println(aniversariantes.size() + " aniversariantes encontrados. Gerando planilha...");
        String filename = "aniversariantes_membros_semana_" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + ".xlsx";
        excelService.gerarPlanilha(filename, aniversariantes);
        System.out.println("Relatório " + filename + " gerado com sucesso em background.");
    }

}