package com.github.josiasdev.RelatorioAniversariantes.service;

import com.github.josiasdev.RelatorioAniversariantes.exception.WhatsAppException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class WhatsAppServiceIntegrationTest {

    @Container
    static WireMockContainer wireMock = new WireMockContainer("wiremock/wiremock:3.5.4")
            .withMappingFromResource("sendMedia-ok", "fixtures/church/mappings-whatsapp/sendMedia-ok.json")
            .withMappingFromResource("sendText-ok", "fixtures/church/mappings-whatsapp/sendText-ok.json")
            .withMappingFromResource("sendText-falha", "fixtures/church/mappings-whatsapp/sendText-falha.json");

    @DynamicPropertySource
    static void whatsappProps(DynamicPropertyRegistry registry) {
        registry.add("whatsapp.api.base-url", wireMock::getBaseUrl);
        registry.add("whatsapp.api.instance", () -> "igreja");
        registry.add("whatsapp.api.key", () -> "chave-teste-123");
        registry.add("whatsapp.destinatario", () -> "5585999999999");
    }

    @Autowired
    private WhatsAppService whatsAppService;

    @Test
    void enviaPdfComHeaderApikeyEcorpoEsperado() throws Exception {
        Path pdf = Files.createTempFile("relatorio_aniversariantes_", ".pdf");
        Files.writeString(pdf, "%PDF-1.4 conteudo fake de teste");

        whatsAppService.enviarRelatorioPdf(pdf.toString());

        String admin = buscarRequisicoesDoWireMock();
        assertThat(admin)
                .contains("/message/sendMedia/igreja")
                .contains("chave-teste-123")
                .contains("mediaMessage")
                .contains("application/pdf");
    }

    @Test
    void enviaMensagemDeTextoParaAlerta() {
        whatsAppService.enviarMensagemTexto("🚨 Falha na geração do relatório de aniversariantes");

        String admin = buscarRequisicoesDoWireMock();
        assertThat(admin)
                .contains("/message/sendText/igreja")
                .contains("Falha na geração do relatório");
    }

    @Test
    void lancaWhatsAppExceptionQuandoApiRetorna500() {
        WhatsAppService servicoInstanciaFalha = new WhatsAppService(
                wireMock.getBaseUrl(), "falha", "chave-teste-123", "5585999999999", new SimpleMeterRegistry());

        assertThatThrownBy(() -> servicoInstanciaFalha.enviarMensagemTexto("teste"))
                .isInstanceOf(WhatsAppException.class)
                .hasMessageContaining("Status: 500");
    }

    private String buscarRequisicoesDoWireMock() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(wireMock.getBaseUrl() + "/__admin/requests"))
                    .GET()
                    .build();
            return HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString())
                    .body();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao consultar admin do WireMock", e);
        }
    }
}
