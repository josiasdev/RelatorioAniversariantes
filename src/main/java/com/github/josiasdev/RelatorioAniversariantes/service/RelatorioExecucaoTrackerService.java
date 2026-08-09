package com.github.josiasdev.RelatorioAniversariantes.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class RelatorioExecucaoTrackerService {

    public enum Status {
        IDLE,
        EM_PROGRESSO,
        SUCESSO,
        ERRO
    }

    @Getter
    private Status status = Status.IDLE;

    @Getter
    private LocalDateTime inicioExecucao;

    @Getter
    private LocalDateTime fimExecucao;

    @Getter
    private String arquivoGerado;

    @Getter
    private String mensagem = "Nenhum processamento executado recentemente.";

    @Getter
    private int totalMembros = 0;

    @Getter
    private int totalCongregados = 0;

    @Getter
    private int totalCasamentos = 0;

    public synchronized void iniciarExecucao() {
        this.status = Status.EM_PROGRESSO;
        this.inicioExecucao = LocalDateTime.now();
        this.fimExecucao = null;
        this.arquivoGerado = null;
        this.mensagem = "O robô está extraindo os dados e gerando o relatório...";
        this.totalMembros = 0;
        this.totalCongregados = 0;
        this.totalCasamentos = 0;
    }

    public synchronized void registrarSucesso(String arquivoGerado, int membros, int congregados, int casamentos) {
        this.status = Status.SUCESSO;
        this.fimExecucao = LocalDateTime.now();
        this.arquivoGerado = arquivoGerado;
        this.totalMembros = membros;
        this.totalCongregados = congregados;
        this.totalCasamentos = casamentos;
        this.mensagem = String.format("Relatório gerado com sucesso (%s). Extraídos %d membros, %d congregados, %d casamentos.",
                arquivoGerado, membros, congregados, casamentos);
    }

    public synchronized void registrarErro(String detalheErro) {
        this.status = Status.ERRO;
        this.fimExecucao = LocalDateTime.now();
        this.mensagem = "Erro durante o processamento do relatório: " + detalheErro;
    }

    public Map<String, Object> obterStatusDetalhado() {
        Map<String, Object> dados = new HashMap<>();
        dados.put("status", status.name());
        dados.put("mensagem", mensagem);
        dados.put("inicioExecucao", inicioExecucao != null ? inicioExecucao.toString() : null);
        dados.put("fimExecucao", fimExecucao != null ? fimExecucao.toString() : null);

        if (inicioExecucao != null) {
            LocalDateTime referenciaFim = fimExecucao != null ? fimExecucao : LocalDateTime.now();
            dados.put("duracaoSegundos", Duration.between(inicioExecucao, referenciaFim).getSeconds());
        } else {
            dados.put("duracaoSegundos", 0);
        }

        dados.put("arquivoGerado", arquivoGerado);

        Map<String, Integer> totais = new HashMap<>();
        totais.put("membros", totalMembros);
        totais.put("congregados", totalCongregados);
        totais.put("casamentos", totalCasamentos);
        dados.put("totais", totais);

        return dados;
    }
}
