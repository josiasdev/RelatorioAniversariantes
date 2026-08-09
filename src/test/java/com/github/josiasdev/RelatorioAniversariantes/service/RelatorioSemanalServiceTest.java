package com.github.josiasdev.RelatorioAniversariantes.service;

import com.github.josiasdev.RelatorioAniversariantes.dto.AniversarianteDTO;
import com.github.josiasdev.RelatorioAniversariantes.dto.CasamentoDTO;
import com.github.josiasdev.RelatorioAniversariantes.dto.DadosRelatorioDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RelatorioSemanalServiceTest {

    @Test
    void extrairDadosDaSemanaConsultaDiaADiaQuandoSemanaTerminaEmOutroMes() throws InterruptedException {
        LocalDate inicio = LocalDate.of(2026, 6, 29);
        LocalDate fim = LocalDate.of(2026, 7, 5);
        FakeWebScraperService webScraperService = new FakeWebScraperService();
        RelatorioSemanalService relatorioSemanalService = new RelatorioSemanalService(webScraperService);

        webScraperService.adicionarRetorno(LocalDate.of(2026, 6, 29), LocalDate.of(2026, 6, 29),
                new DadosRelatorioDTO(
                        List.of(aniversariante("29")),
                        List.of(aniversariante("29")),
                        List.of(casamento("29"))
                ));
        webScraperService.adicionarRetorno(LocalDate.of(2026, 6, 30), LocalDate.of(2026, 6, 30),
                new DadosRelatorioDTO(
                        List.of(aniversariante("30")),
                        List.of(aniversariante("30")),
                        List.of(casamento("30"))
                ));
        webScraperService.adicionarRetorno(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 1),
                new DadosRelatorioDTO(
                        List.of(aniversariante("1")),
                        List.of(aniversariante("1")),
                        List.of(casamento("1"))
                ));
        webScraperService.adicionarRetorno(LocalDate.of(2026, 7, 2), LocalDate.of(2026, 7, 2),
                new DadosRelatorioDTO(
                        List.of(aniversariante("2")),
                        List.of(),
                        List.of()
                ));
        webScraperService.adicionarRetorno(LocalDate.of(2026, 7, 3), LocalDate.of(2026, 7, 3),
                new DadosRelatorioDTO(
                        List.of(aniversariante("3")),
                        List.of(),
                        List.of()
                ));
        webScraperService.adicionarRetorno(LocalDate.of(2026, 7, 4), LocalDate.of(2026, 7, 4),
                new DadosRelatorioDTO(
                        List.of(aniversariante("4")),
                        List.of(),
                        List.of()
                ));
        webScraperService.adicionarRetorno(LocalDate.of(2026, 7, 5), LocalDate.of(2026, 7, 5),
                new DadosRelatorioDTO(
                        List.of(aniversariante("5")),
                        List.of(),
                        List.of(casamento("5"))
                ));

        DadosRelatorioDTO dados = relatorioSemanalService.extrairDadosDaSemana(inicio, fim);

        assertThat(dados.getMembros())
                .extracting(AniversarianteDTO::getDia)
                .containsExactly("29", "30", "1", "2", "3", "4", "5");
        assertThat(dados.getCongregados())
                .extracting(AniversarianteDTO::getDia)
                .containsExactly("29", "30", "1");
        assertThat(dados.getCasamentos())
                .extracting(CasamentoDTO::getDia)
                .containsExactly("29", "30", "1", "5");

        assertThat(webScraperService.getChamadas()).containsExactly(
                "2026-06-29/2026-06-29",
                "2026-06-30/2026-06-30",
                "2026-07-01/2026-07-01",
                "2026-07-02/2026-07-02",
                "2026-07-03/2026-07-03",
                "2026-07-04/2026-07-04",
                "2026-07-05/2026-07-05"
        );
    }

    @Test
    void extrairDadosDaSemanaFazUmaConsultaQuandoSemanaFicaNoMesmoMes() throws InterruptedException {
        LocalDate inicio = LocalDate.of(2026, 7, 6);
        LocalDate fim = LocalDate.of(2026, 7, 12);
        FakeWebScraperService webScraperService = new FakeWebScraperService();
        RelatorioSemanalService relatorioSemanalService = new RelatorioSemanalService(webScraperService);

        webScraperService.adicionarRetorno(inicio, fim,
                new DadosRelatorioDTO(
                        List.of(aniversariante("12"), aniversariante("6")),
                        List.of(),
                        List.of()
                ));

        DadosRelatorioDTO dados = relatorioSemanalService.extrairDadosDaSemana(inicio, fim);

        assertThat(dados.getMembros())
                .extracting(AniversarianteDTO::getDia)
                .containsExactly("6", "12");

        assertThat(webScraperService.getChamadas()).containsExactly("2026-07-06/2026-07-12");
    }

    private AniversarianteDTO aniversariante(String dia) {
        return new AniversarianteDTO(dia, "Nome " + dia, "30", "Sede");
    }

    private CasamentoDTO casamento(String dia) {
        return new CasamentoDTO(dia, "Casal " + dia, dia + "/01/2000", "Sede");
    }

    private static class FakeWebScraperService extends WebScraperService {

        private final Map<String, DadosRelatorioDTO> retornos = new HashMap<>();
        private final List<String> chamadas = new ArrayList<>();

        @Override
        public DadosRelatorioDTO extrairTodosOsDados(LocalDate startOfWeek, LocalDate endOfWeek) {
            String chave = criarChave(startOfWeek, endOfWeek);
            chamadas.add(chave);
            return retornos.get(chave);
        }

        void adicionarRetorno(LocalDate inicio, LocalDate fim, DadosRelatorioDTO dados) {
            retornos.put(criarChave(inicio, fim), dados);
        }

        List<String> getChamadas() {
            return chamadas;
        }

        private String criarChave(LocalDate inicio, LocalDate fim) {
            return inicio + "/" + fim;
        }
    }
}
