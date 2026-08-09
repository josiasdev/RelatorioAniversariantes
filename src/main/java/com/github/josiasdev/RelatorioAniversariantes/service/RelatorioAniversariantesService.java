package com.github.josiasdev.RelatorioAniversariantes.service;

import com.github.josiasdev.RelatorioAniversariantes.dto.CasamentoDTO;
import com.github.josiasdev.RelatorioAniversariantes.dto.DadosRelatorioDTO;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class RelatorioAniversariantesService {

    private final RelatorioSemanalService relatorioSemanalService;
    private final PdfService pdfService;
    private final WhatsAppService whatsAppService;
    private final RelatorioExecucaoTrackerService trackerService;

    public RelatorioAniversariantesService(
            RelatorioSemanalService relatorioSemanalService,
            PdfService pdfService,
            WhatsAppService whatsAppService,
            RelatorioExecucaoTrackerService trackerService) {
        this.relatorioSemanalService = relatorioSemanalService;
        this.pdfService = pdfService;
        this.whatsAppService = whatsAppService;
        this.trackerService = trackerService;
    }

    @Scheduled(cron = "0 0 8 * * MON", zone = "America/Fortaleza")
    @Async
    public void gerarRelatorioAniversariantes() {
        trackerService.iniciarExecucao();
        try {
            LocalDate today = LocalDate.now();
            LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            String dataFormatada = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

            System.out.println("Iniciando extração geral...");

            DadosRelatorioDTO todosOsDados = relatorioSemanalService.extrairDadosDaSemana(startOfWeek, endOfWeek);

            if (todosOsDados.getCasamentos() != null) {
                List<CasamentoDTO> casamentosUnicos = removerCasaisRepetidos(todosOsDados.getCasamentos());
                todosOsDados.setCasamentos(casamentosUnicos);
                relatorioSemanalService.ordenarDadosPorSemana(todosOsDados, startOfWeek);
            }

            String filename = "relatorio_aniversariantes_" + dataFormatada + ".pdf";

            String caminhoGerado = pdfService.gerarRelatorioPdf(filename, todosOsDados, startOfWeek, endOfWeek);

            System.out.println("Sucesso! Relatório PDF gerado: " + caminhoGerado);

            whatsAppService.enviarRelatorioPdf(caminhoGerado);

            int qtdMembros = todosOsDados.getMembros() != null ? todosOsDados.getMembros().size() : 0;
            int qtdCongregados = todosOsDados.getCongregados() != null ? todosOsDados.getCongregados().size() : 0;
            int qtdCasamentos = todosOsDados.getCasamentos() != null ? todosOsDados.getCasamentos().size() : 0;

            trackerService.registrarSucesso(caminhoGerado, qtdMembros, qtdCongregados, qtdCasamentos);

        } catch (Exception e) {
            System.err.println("Erro durante a geração dos relatórios: " + e.getMessage());
            trackerService.registrarErro(e.getMessage());
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
