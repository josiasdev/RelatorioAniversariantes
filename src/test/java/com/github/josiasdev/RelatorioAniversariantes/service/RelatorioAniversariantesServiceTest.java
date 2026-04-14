package com.github.josiasdev.RelatorioAniversariantes.service;

import com.github.josiasdev.RelatorioAniversariantes.dto.AniversarianteDTO;
import com.github.josiasdev.RelatorioAniversariantes.dto.CasamentoDTO;
import com.github.josiasdev.RelatorioAniversariantes.dto.DadosRelatorioDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioAniversariantesServiceTest {

    @Mock
    private WebScraperService webScraperService;

    @Mock
    private PdfService pdfService;

    @InjectMocks
    private RelatorioAniversariantesService relatorioAniversariantesService;

    @Test
    @DisplayName("Deve remover casais duplicados e ordenar todas as listas por dia")
    void deveFiltrarCasaisDuplicadosEOrdenarPorDia() throws Exception {
        List<AniversarianteDTO> membros = new ArrayList<>(List.of(
                new AniversarianteDTO("10", "João", "30", "Sede"),
                new AniversarianteDTO("02", "Maria", "25", "Sede")
        ));

        List<AniversarianteDTO> congregados = new ArrayList<>();

        List<CasamentoDTO> casamentos = new ArrayList<>(List.of(
                new CasamentoDTO("16", "FRANCISCO JOSÉ & VANDERLENY DA SILVA", "16/04/1992"),
                new CasamentoDTO("16", "VANDERLENY DA SILVA & FRANCISCO JOSÉ", "16/04/1992"),
                new CasamentoDTO("05", "ANA & CARLOS", "01/04/2000")
        ));

        DadosRelatorioDTO dadosMocados = new DadosRelatorioDTO(membros, congregados, casamentos);

        when(webScraperService.extrairTodosOsDados(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(dadosMocados);

        relatorioAniversariantesService.gerarRelatorioAniversariantes();

        ArgumentCaptor<DadosRelatorioDTO> captor = ArgumentCaptor.forClass(DadosRelatorioDTO.class);
        verify(pdfService).gerarRelatorioPdf(anyString(), captor.capture(), any(LocalDate.class), any(LocalDate.class));

        DadosRelatorioDTO dadosProcessados = captor.getValue();

        assertEquals("02", dadosProcessados.getMembros().get(0).getDia(), "O dia 02 deve vir antes do 10");
        assertEquals("10", dadosProcessados.getMembros().get(1).getDia());

        List<CasamentoDTO> casamentosProcessados = dadosProcessados.getCasamentos();

        assertEquals(2, casamentosProcessados.size(), "Deve remover o 1 casal duplicado, sobrando apenas 2");

        assertEquals("05", casamentosProcessados.get(0).getDia(), "O casamento do dia 05 deve ser o primeiro");
        assertEquals("16", casamentosProcessados.get(1).getDia());

        assertEquals("FRANCISCO JOSÉ & VANDERLENY DA SILVA", casamentosProcessados.get(1).getCasal(),
                "O nome do casal deve ser mantido na ordem alfabética calculada");
    }
}