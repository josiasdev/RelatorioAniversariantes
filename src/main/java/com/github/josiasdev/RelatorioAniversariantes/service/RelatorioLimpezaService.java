package com.github.josiasdev.RelatorioAniversariantes.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

@Slf4j
@Service
public class RelatorioLimpezaService {

    @Value("${app.reports.output-directory:relatorios}")
    private String outputDirectory;

    @Value("${app.reports.retention-days:30}")
    private int retentionDays;

    @Scheduled(cron = "0 0 3 * * *")
    public void limparRelatoriosAntigos() {
        Path pasta = Paths.get(outputDirectory);
        if (!Files.exists(pasta) || !Files.isDirectory(pasta)) {
            return;
        }

        Instant limite = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        log.info("Iniciando limpeza de relatórios anteriores a {} dias na pasta '{}'.", retentionDays, outputDirectory);

        try (Stream<Path> arquivos = Files.list(pasta)) {
            arquivos.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".pdf"))
                    .forEach(path -> {
                        try {
                            Instant dataModificacao = Files.getLastModifiedTime(path).toInstant();
                            if (dataModificacao.isBefore(limite)) {
                                Files.delete(path);
                                log.info("Relatório antigo removido: {}", path.getFileName());
                            }
                        } catch (IOException e) {
                            log.error("Erro ao tentar remover relatório antigo {}: {}", path.getFileName(), e.getMessage());
                        }
                    });
        } catch (IOException e) {
            log.error("Erro ao listar pasta de relatórios para limpeza: {}", e.getMessage());
        }
    }
}
