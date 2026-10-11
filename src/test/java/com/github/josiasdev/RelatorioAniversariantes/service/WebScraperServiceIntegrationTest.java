package com.github.josiasdev.RelatorioAniversariantes.service;

import com.github.josiasdev.RelatorioAniversariantes.dto.AniversarianteDTO;
import com.github.josiasdev.RelatorioAniversariantes.dto.CasamentoDTO;
import com.github.josiasdev.RelatorioAniversariantes.dto.DadosRelatorioDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class WebScraperServiceIntegrationTest {

    @Container
    static WireMockContainer wireMock = new WireMockContainer("wiremock/wiremock:3.5.4")
            .withFileFromResource("login.html", "fixtures/church/login.html")
            .withFileFromResource("aniversarios.html", "fixtures/church/aniversarios.html")
            .withFileFromResource("casamentos.html", "fixtures/church/casamentos.html")
            .withMappingFromResource("frmlogin-success", "fixtures/church/mappings/frmlogin-success.json")
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
    void extraiMembrosCongregadosECasamentosAPartirDasFixtures() throws InterruptedException {
        LocalDate inicio = LocalDate.of(2026, 10, 5);
        LocalDate fim = LocalDate.of(2026, 10, 11);

        DadosRelatorioDTO dados = webScraperService.extrairTodosOsDados(inicio, fim);

        assertThat(dados.getMembros())
                .extracting(AniversarianteDTO::getNome)
                .contains("Maria Silva", "João Souza", "Ana Clara");
        assertThat(dados.getMembros())
                .extracting(AniversarianteDTO::getCongregacao)
                .contains("Centro", "Norte", "Sede");

        assertThat(dados.getCongregados())
                .extracting(AniversarianteDTO::getNome)
                .contains("Maria Silva", "João Souza", "Ana Clara");

        assertThat(dados.getCasamentos())
                .extracting(CasamentoDTO::getCasal)
                .contains("José & Maria", "Ana & Pedro", "Bia & Léo");
        assertThat(dados.getCasamentos())
                .extracting(CasamentoDTO::getCongregacao)
                .containsExactly("Centro", "Centro", "Norte");
    }
}
