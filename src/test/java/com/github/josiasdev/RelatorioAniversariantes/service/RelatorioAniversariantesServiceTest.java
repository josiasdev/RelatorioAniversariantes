package com.github.josiasdev.RelatorioAniversariantes.service;

import com.github.josiasdev.RelatorioAniversariantes.dto.AniversarianteDTO;
import com.github.josiasdev.RelatorioAniversariantes.dto.DadosRelatorioDTO;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RelatorioAniversariantesServiceTest {

    private RelatorioSemanalService relatorioSemanalService;
    private PdfService pdfService;
    private WhatsAppService whatsAppService;
    private RelatorioExecucaoTrackerService trackerService;
    private SimpleMeterRegistry meterRegistry;
    private RelatorioAniversariantesService service;

    @BeforeEach
    void setUp() {
        relatorioSemanalService = mock(RelatorioSemanalService.class);
        pdfService = mock(PdfService.class);
        whatsAppService = mock(WhatsAppService.class);
        trackerService = mock(RelatorioExecucaoTrackerService.class);
        meterRegistry = new SimpleMeterRegistry();
        service = new RelatorioAniversariantesService(
                relatorioSemanalService, pdfService, whatsAppService, trackerService, meterRegistry);
    }

    @Test
    void registraSucessoEGeraPdfQuandoFluxoCompleta() throws InterruptedException, IOException {
        DadosRelatorioDTO dados = new DadosRelatorioDTO(
                List.of(new AniversarianteDTO("12", "Maria", "35", "Centro")),
                List.of(),
                List.of());
        when(relatorioSemanalService.extrairDadosDaSemana(any(), any())).thenReturn(dados);
        when(pdfService.gerarRelatorioPdf(anyString(), any(), any(), any())).thenReturn("relatorios/teste.pdf");

        service.gerarRelatorioAgendado();

        verify(trackerService).registrarSucesso(eq("relatorios/teste.pdf"), eq(1), eq(0), eq(0));
        verify(whatsAppService).enviarRelatorioPdf("relatorios/teste.pdf");
        verify(trackerService, never()).registrarErro(any());
        verify(whatsAppService, never()).enviarMensagemTexto(any());
        assertThat(meterRegistry.find("relatorio.geracao.execucoes").tag("outcome", "sucesso").counter()).isNotNull();
    }

    @Test
    void enviaAlertaWhatsappQuandoScrapingFalha() throws InterruptedException, IOException {
        when(relatorioSemanalService.extrairDadosDaSemana(any(), any()))
                .thenThrow(new RuntimeException("Church fora do ar"));
        when(whatsAppService.estaConfigurado()).thenReturn(true);

        service.gerarRelatorioAgendado();

        verify(trackerService).registrarErro("Church fora do ar");
        verify(whatsAppService).enviarMensagemTexto(contains("Falha na geração do relatório"));
        verify(whatsAppService, never()).enviarRelatorioPdf(any());
        verify(pdfService, never()).gerarRelatorioPdf(anyString(), any(), any(), any());
        assertThat(meterRegistry.find("relatorio.geracao.execucoes").tag("outcome", "erro").counter()).isNotNull();
    }

    @Test
    void naoTentaAlertarQuandoWhatsappNaoEstaConfigurado() throws InterruptedException {
        when(relatorioSemanalService.extrairDadosDaSemana(any(), any()))
                .thenThrow(new RuntimeException("boom"));
        when(whatsAppService.estaConfigurado()).thenReturn(false);

        service.gerarRelatorioAgendado();

        verify(whatsAppService, never()).enviarMensagemTexto(any());
        verify(trackerService).registrarErro("boom");
    }

    @Test
    void falhaNoEnvioDoPdfNaoImpedeRegistroDeErroENaoSobrescreveErroOriginal() throws InterruptedException, IOException {
        DadosRelatorioDTO dados = new DadosRelatorioDTO(List.of(), List.of(), List.of());
        when(relatorioSemanalService.extrairDadosDaSemana(any(), any())).thenReturn(dados);
        when(pdfService.gerarRelatorioPdf(anyString(), any(), any(), any())).thenReturn("relatorios/teste.pdf");
        when(whatsAppService.estaConfigurado()).thenReturn(true);
        org.mockito.Mockito.doThrow(new RuntimeException("Evolution API indisponível"))
                .when(whatsAppService).enviarRelatorioPdf(anyString());

        service.gerarRelatorioAgendado();

        verify(trackerService).registrarErro("Evolution API indisponível");
        verify(whatsAppService).enviarMensagemTexto(contains("Falha na geração do relatório"));
    }

    @Test
    void segundoDisparoEhIgnoradoQuandoJaExisteExecucaoEmAndamento() throws Exception {
        CountDownLatch iniciado = new CountDownLatch(1);
        CountDownLatch liberar = new CountDownLatch(1);

        when(relatorioSemanalService.extrairDadosDaSemana(any(), any())).thenAnswer(invocation -> {
            iniciado.countDown();
            liberar.await(10, TimeUnit.SECONDS);
            return new DadosRelatorioDTO(List.of(), List.of(), List.of());
        });
        when(pdfService.gerarRelatorioPdf(anyString(), any(), any(), any())).thenReturn("relatorios/teste.pdf");

        Thread execucao = new Thread(service::gerarRelatorioAgendado);
        execucao.start();

        assertThat(iniciado.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(service.estaEmAndamento()).isTrue();

        service.gerarRelatorioAssincrono();

        verify(relatorioSemanalService, times(1)).extrairDadosDaSemana(any(), any());

        liberar.countDown();
        execucao.join(10_000);

        assertThat(service.estaEmAndamento()).isFalse();
    }
}
