package com.github.josiasdev.RelatorioAniversariantes.service;

import com.github.josiasdev.RelatorioAniversariantes.dto.CasamentoDTO;
import com.github.josiasdev.RelatorioAniversariantes.dto.DadosRelatorioDTO;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class RelatorioAniversariantesService {

    private final RelatorioSemanalService relatorioSemanalService;
    private final PdfService pdfService;
    private final WhatsAppService whatsAppService;
    private final RelatorioExecucaoTrackerService trackerService;
    private final MeterRegistry meterRegistry;
    private final AtomicBoolean emExecucao = new AtomicBoolean(false);

    public RelatorioAniversariantesService(
            RelatorioSemanalService relatorioSemanalService,
            PdfService pdfService,
            WhatsAppService whatsAppService,
            RelatorioExecucaoTrackerService trackerService,
            MeterRegistry meterRegistry) {
        this.relatorioSemanalService = relatorioSemanalService;
        this.pdfService = pdfService;
        this.whatsAppService = whatsAppService;
        this.trackerService = trackerService;
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(cron = "0 0 8 * * MON", zone = "America/Fortaleza")
    public void gerarRelatorioAgendado() {
        if (!emExecucao.compareAndSet(false, true)) {
            log.warn("Execução agendada ignorada: já existe uma geração de relatório em andamento.");
            return;
        }
        try {
            executarGeracao();
        } finally {
            emExecucao.set(false);
        }
    }

    @Async("relatorioExecutor")
    public void gerarRelatorioAssincrono() {
        if (!emExecucao.compareAndSet(false, true)) {
            log.warn("Disparo assíncrono ignorado: já existe uma geração de relatório em andamento.");
            return;
        }
        try {
            executarGeracao();
        } finally {
            emExecucao.set(false);
        }
    }

    public boolean estaEmAndamento() {
        return emExecucao.get();
    }

    private void executarGeracao() {
        long inicioNano = System.nanoTime();
        trackerService.iniciarExecucao();
        try {
            LocalDate today = LocalDate.now();
            LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            String dataFormatada = today.format(DateTimeFormatter.ISO_LOCAL_DATE);

            log.info("Iniciando extração geral...");

            DadosRelatorioDTO todosOsDados = relatorioSemanalService.extrairDadosDaSemana(startOfWeek, endOfWeek);

            if (todosOsDados.getCasamentos() != null) {
                List<CasamentoDTO> casamentosUnicos = removerCasaisRepetidos(todosOsDados.getCasamentos());
                todosOsDados.setCasamentos(casamentosUnicos);
                relatorioSemanalService.ordenarDadosPorSemana(todosOsDados, startOfWeek);
            }

            String filename = "relatorio_aniversariantes_" + dataFormatada + ".pdf";

            String caminhoGerado = pdfService.gerarRelatorioPdf(filename, todosOsDados, startOfWeek, endOfWeek);

            log.info("Sucesso! Relatório PDF gerado: {}", caminhoGerado);

            whatsAppService.enviarRelatorioPdf(caminhoGerado);

            int qtdMembros = todosOsDados.getMembros() != null ? todosOsDados.getMembros().size() : 0;
            int qtdCongregados = todosOsDados.getCongregados() != null ? todosOsDados.getCongregados().size() : 0;
            int qtdCasamentos = todosOsDados.getCasamentos() != null ? todosOsDados.getCasamentos().size() : 0;

            trackerService.registrarSucesso(caminhoGerado, qtdMembros, qtdCongregados, qtdCasamentos);
            meterRegistry.counter("relatorio.geracao.execucoes", "outcome", "sucesso").increment();

        } catch (Exception e) {
            log.error("Erro durante a geração dos relatórios: {}", e.getMessage(), e);
            trackerService.registrarErro(e.getMessage());
            meterRegistry.counter("relatorio.geracao.execucoes", "outcome", "erro").increment();
            enviarAlertaDeFalha(e);
        } finally {
            long duracaoNanos = System.nanoTime() - inicioNano;
            meterRegistry.timer("relatorio.geracao.tempo").record(duracaoNanos, TimeUnit.NANOSECONDS);
        }
    }

    private void enviarAlertaDeFalha(Exception causa) {
        if (!whatsAppService.estaConfigurado()) {
            log.warn("Alerta de falha não enviado: WhatsApp não configurado.");
            return;
        }
        try {
            String horario = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            String texto = """
                    🚨 *Falha na geração do relatório de aniversariantes*
                    Horário: %s
                    Erro: %s
                    Consulte o detalhamento em GET /relatorios/status"""
                    .formatted(horario, causa.getMessage());
            whatsAppService.enviarMensagemTexto(texto);
        } catch (Exception e) {
            log.error("Não foi possível enviar o alerta de falha via WhatsApp: {}", e.getMessage(), e);
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

        log.info("Casamentos extraídos: {} | Casais únicos: {}", casamentos.size(), listaUnica.size());
        return listaUnica;
    }
}
