package com.github.josiasdev.RelatorioAniversariantes.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RelatorioExecucaoTrackerServiceTest {

    @Test
    void deveIniciarExecucaoComStatusEmProgresso() {
        RelatorioExecucaoTrackerService tracker = new RelatorioExecucaoTrackerService();
        tracker.iniciarExecucao();

        assertThat(tracker.getStatus()).isEqualTo(RelatorioExecucaoTrackerService.Status.EM_PROGRESSO);
        assertThat(tracker.getInicioExecucao()).isNotNull();
        assertThat(tracker.getFimExecucao()).isNull();
    }

    @Test
    void deveRegistrarSucessoETotais() {
        RelatorioExecucaoTrackerService tracker = new RelatorioExecucaoTrackerService();
        tracker.iniciarExecucao();
        tracker.registrarSucesso("relatorios/relatorio_aniversariantes_2026-08-09.pdf", 10, 5, 2);

        assertThat(tracker.getStatus()).isEqualTo(RelatorioExecucaoTrackerService.Status.SUCESSO);
        assertThat(tracker.getFimExecucao()).isNotNull();
        assertThat(tracker.getArquivoGerado()).isEqualTo("relatorios/relatorio_aniversariantes_2026-08-09.pdf");
        assertThat(tracker.getTotalMembros()).isEqualTo(10);
        assertThat(tracker.getTotalCongregados()).isEqualTo(5);
        assertThat(tracker.getTotalCasamentos()).isEqualTo(2);

        Map<String, Object> statusDetalhado = tracker.obterStatusDetalhado();
        assertThat(statusDetalhado.get("status")).isEqualTo("SUCESSO");
        assertThat(statusDetalhado.get("arquivoGerado")).isEqualTo("relatorios/relatorio_aniversariantes_2026-08-09.pdf");

        @SuppressWarnings("unchecked")
        Map<String, Integer> totais = (Map<String, Integer>) statusDetalhado.get("totais");
        assertThat(totais).containsEntry("membros", 10);
        assertThat(totais).containsEntry("congregados", 5);
        assertThat(totais).containsEntry("casamentos", 2);
    }

    @Test
    void deveRegistrarErro() {
        RelatorioExecucaoTrackerService tracker = new RelatorioExecucaoTrackerService();
        tracker.iniciarExecucao();
        tracker.registrarErro("Falha de conexão com a plataforma Church");

        assertThat(tracker.getStatus()).isEqualTo(RelatorioExecucaoTrackerService.Status.ERRO);
        assertThat(tracker.getMensagem()).contains("Falha de conexão com a plataforma Church");
    }
}
