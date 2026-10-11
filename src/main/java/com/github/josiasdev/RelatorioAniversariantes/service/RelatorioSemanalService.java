package com.github.josiasdev.RelatorioAniversariantes.service;

import com.github.josiasdev.RelatorioAniversariantes.dto.AniversarianteDTO;
import com.github.josiasdev.RelatorioAniversariantes.dto.CasamentoDTO;
import com.github.josiasdev.RelatorioAniversariantes.dto.DadosRelatorioDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class RelatorioSemanalService {

    private final WebScraperService webScraperService;

    public RelatorioSemanalService(WebScraperService webScraperService) {
        this.webScraperService = webScraperService;
    }

    public DadosRelatorioDTO extrairDadosDaSemana(LocalDate inicio, LocalDate fim) throws InterruptedException {
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("As datas de inicio e fim da semana sao obrigatorias.");
        }

        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("A data final da semana nao pode ser anterior a data inicial.");
        }

        DadosRelatorioDTO dadosConsolidados = new DadosRelatorioDTO(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        if (inicio.getMonth() == fim.getMonth() && inicio.getYear() == fim.getYear()) {
            extrairEAdicionarDados(dadosConsolidados, inicio, fim);
        } else {
            LocalDate dia = inicio;
            while (!dia.isAfter(fim)) {
                extrairEAdicionarDados(dadosConsolidados, dia, dia);
                dia = dia.plusDays(1);
            }
        }

        ordenarDadosPorSemana(dadosConsolidados, inicio);
        return dadosConsolidados;
    }

    public void ordenarDadosPorSemana(DadosRelatorioDTO dados, LocalDate inicioSemana) {
        Comparator<String> comparadorDia = Comparator.comparingInt(dia -> calcularOrdemDoDia(dia, inicioSemana));

        if (dados.getMembros() != null) {
            dados.getMembros().sort(Comparator.comparing(AniversarianteDTO::getDia, comparadorDia));
        }

        if (dados.getCongregados() != null) {
            dados.getCongregados().sort(Comparator.comparing(AniversarianteDTO::getDia, comparadorDia));
        }

        if (dados.getCasamentos() != null) {
            dados.getCasamentos().sort(Comparator.comparing(CasamentoDTO::getDia, comparadorDia));
        }
    }

    private void adicionarDados(DadosRelatorioDTO destino, DadosRelatorioDTO origem) {
        if (origem == null) {
            return;
        }

        adicionarLista(destino.getMembros(), origem.getMembros());
        adicionarLista(destino.getCongregados(), origem.getCongregados());
        adicionarLista(destino.getCasamentos(), origem.getCasamentos());
    }

    private void extrairEAdicionarDados(DadosRelatorioDTO dadosConsolidados, LocalDate inicio, LocalDate fim) throws InterruptedException {
        log.info("Extraindo periodo semanal: {} a {}", inicio, fim);
        DadosRelatorioDTO dadosPeriodo = webScraperService.extrairTodosOsDados(inicio, fim);
        logarTotaisPeriodo(inicio, fim, dadosPeriodo);
        adicionarDados(dadosConsolidados, dadosPeriodo);
    }

    private void logarTotaisPeriodo(LocalDate inicio, LocalDate fim, DadosRelatorioDTO dados) {
        if (dados == null) {
            log.info("Periodo {} a {} sem dados retornados.", inicio, fim);
            return;
        }

        log.info(
                "Periodo {} a {} retornou: {} membros, {} congregados, {} casamentos.",
                inicio,
                fim,
                contar(dados.getMembros()),
                contar(dados.getCongregados()),
                contar(dados.getCasamentos())
        );
    }

    private <T> void adicionarLista(List<T> destino, List<T> origem) {
        if (origem != null) {
            destino.addAll(origem);
        }
    }

    private int contar(List<?> lista) {
        return lista == null ? 0 : lista.size();
    }

    private int calcularOrdemDoDia(String dia, LocalDate inicioSemana) {
        int numeroDia = Integer.parseInt(dia);

        if (inicioSemana.getDayOfMonth() > numeroDia) {
            return inicioSemana.lengthOfMonth() - inicioSemana.getDayOfMonth() + numeroDia;
        }

        return numeroDia - inicioSemana.getDayOfMonth();
    }
}
