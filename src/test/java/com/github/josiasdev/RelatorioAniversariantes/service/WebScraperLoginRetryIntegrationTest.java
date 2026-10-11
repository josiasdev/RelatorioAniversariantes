package com.github.josiasdev.RelatorioAniversariantes.service;

import com.github.josiasdev.RelatorioAniversariantes.dto.DadosRelatorioDTO;
import com.jayway.jsonpath.JsonPath;
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
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class WebScraperLoginRetryIntegrationTest {

    @Container
    static WireMockContainer wireMock = new WireMockContainer("wiremock/wiremock:3.5.4")
            .withFileFromResource("login.html", "fixtures/church/login.html")
            .withFileFromResource("aniversarios.html", "fixtures/church/aniversarios.html")
            .withFileFromResource("casamentos.html", "fixtures/church/casamentos.html")
            .withMappingFromResource("frmlogin-scenario-500", "fixtures/church/mappings-retry/frmlogin-scenario-500.json")
            .withMappingFromResource("frmlogin-scenario-200", "fixtures/church/mappings-retry/frmlogin-scenario-200.json")
            .withMappingFromResource("ctrtipoaniversario", "fixtures/church/mappings/ctrtipoaniversario.json")
            .withMappingFromResource("rptCasaAniv", "fixtures/church/mappings/rptCasaAniv.json");

    @DynamicPropertySource
    static void scraperUrls(DynamicPropertyRegistry registry) {
        registry.add("app.scraper.urls.login", () -> wireMock.getUrl("/frmlogin/"));
        registry.add("app.scraper.urls.aniversarios", () -> wireMock.getUrl("/ctrtipoaniversario/ctrtipoaniversario.php"));
        registry.add("app.scraper.urls.casamentos", () -> wireMock.getUrl("/rptCasaAniv/rptCasaAniv.php"));
    }

    @Autowired
    private WebScraperService webScraperService;

    @Test
    void loginFalhaNaPrimeiraTentativaESucessoNoRetry() throws Exception {
        LocalDate inicio = LocalDate.of(2026, 10, 5);
        LocalDate fim = LocalDate.of(2026, 10, 11);

        DadosRelatorioDTO dados = webScraperService.extrairTodosOsDados(inicio, fim);

        assertThat(dados.getMembros()).isNotEmpty();
        assertThat(dados.getCasamentos()).isNotEmpty();
        assertThat(contarRequisicoesLogin()).isEqualTo(2);
    }

    private int contarRequisicoesLogin() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(wireMock.getBaseUrl() + "/__admin/requests"))
                .GET()
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        List<?> requisicoesLogin = JsonPath.read(response.body(), "$.requests[?(@.request.url == '/frmlogin/')]");
        return requisicoesLogin.size();
    }
}
