package com.github.josiasdev.RelatorioAniversariantes.controller;

import com.github.josiasdev.RelatorioAniversariantes.service.RelatorioAniversariantesService;
import com.github.josiasdev.RelatorioAniversariantes.service.RelatorioExecucaoTrackerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RelatorioAniversariantesController.class)
class RelatorioAniversariantesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RelatorioAniversariantesService relatorioAniversariantesService;

    @MockitoBean
    private RelatorioExecucaoTrackerService trackerService;

    @Test
    void deveDispararGeracaoDeRelatorioERetornarAccepted() throws Exception {
        mockMvc.perform(get("/relatorios/gerarAniversariantes"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("Processamento iniciado"));

        verify(relatorioAniversariantesService).gerarRelatorioAniversariantes();
    }

    @Test
    void deveRetornarStatusDoTracker() throws Exception {
        when(trackerService.obterStatusDetalhado()).thenReturn(Map.of(
                "status", "SUCESSO",
                "mensagem", "Relatório gerado com sucesso",
                "arquivoGerado", "relatorios/relatorio_aniversariantes_2026-08-09.pdf"
        ));

        mockMvc.perform(get("/relatorios/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCESSO"))
                .andExpect(jsonPath("$.arquivoGerado").value("relatorios/relatorio_aniversariantes_2026-08-09.pdf"));
    }
}
