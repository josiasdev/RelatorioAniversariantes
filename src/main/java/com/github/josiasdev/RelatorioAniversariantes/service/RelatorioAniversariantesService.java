package com.github.josiasdev.RelatorioAniversariantes.service;

import com.github.josiasdev.RelatorioAniversariantes.dto.CasamentoDTO;
import com.github.josiasdev.RelatorioAniversariantes.dto.DadosRelatorioDTO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RelatorioAniversariantesService {

    private final WebScraperService webScraperService;
    private final PdfService pdfService;

    public RelatorioAniversariantesService(WebScraperService webScraperService, PdfService pdfService) {
        this.webScraperService = webScraperService;
        this.pdfService = pdfService;
    }

    @Async
    public void gerarRelatorioAniversariantes() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            String dataFormatada = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

            System.out.println("Iniciando extração geral...");

            DadosRelatorioDTO todosOsDados = webScraperService.extrairTodosOsDados(startOfWeek, endOfWeek);

            todosOsDados.getMembros().sort((a, b) ->
                    Integer.compare(Integer.parseInt(a.getDia()), Integer.parseInt(b.getDia())));

            todosOsDados.getCongregados().sort((a, b) ->
                    Integer.compare(Integer.parseInt(a.getDia()), Integer.parseInt(b.getDia())));

            todosOsDados.getCasamentos().sort((a, b) ->
                    Integer.compare(Integer.parseInt(a.getDia()), Integer.parseInt(b.getDia())));
            if (todosOsDados.getCasamentos() != null) {
                List<CasamentoDTO> casamentosUnicos = removerCasaisRepetidos(todosOsDados.getCasamentos());
                todosOsDados.setCasamentos(casamentosUnicos);
            }

            String filename = "relatorio_aniversariantes_" + dataFormatada + ".pdf";

            pdfService.gerarRelatorioPdf(filename, todosOsDados, startOfWeek, endOfWeek);

            System.out.println("Sucesso! Relatório PDF gerado: " + filename);

        } catch (Exception e) {
            System.err.println("Erro durante a geração dos relatórios: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<CasamentoDTO> removerCasaisRepetidos(List<CasamentoDTO> casamentos) {
        Set<String> nomesNormalizados = new HashSet<>();
        List<CasamentoDTO> listaUnica = new ArrayList<>();

        for (CasamentoDTO c : casamentos) {
            String[] partes = c.getCasal().split(" & ");
            String nomePadrao = c.getCasal();

            if (partes.length == 2) {
                String conjuge1 = partes[0].trim();
                String conjuge2 = partes[1].trim();

                if (conjuge1.compareTo(conjuge2) > 0) {
                    nomePadrao = conjuge2 + " & " + conjuge1;
                } else {
                    nomePadrao = conjuge1 + " & " + conjuge2;
                }
            }

            if (nomesNormalizados.add(nomePadrao)) {
                c.setCasal(nomePadrao);
                listaUnica.add(c);
            }
        }

        System.out.println("Casamentos extraídos: " + casamentos.size() + " | Casais únicos: " + listaUnica.size());
        return listaUnica;
    }
}