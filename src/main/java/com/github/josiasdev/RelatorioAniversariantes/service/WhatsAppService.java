package com.github.josiasdev.RelatorioAniversariantes.service;

import com.github.josiasdev.RelatorioAniversariantes.exception.WhatsAppException;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;

@Slf4j
@Service
public class WhatsAppService {

    private final String baseUrl;
    private final String instance;
    private final String apiKey;
    private final String numeroDestinatario;
    private final HttpClient httpClient;
    private final Counter enviosComSucesso;
    private final Counter enviosComFalha;

    public WhatsAppService(
            @Value("${whatsapp.api.base-url}") String baseUrl,
            @Value("${whatsapp.api.instance}") String instance,
            @Value("${whatsapp.api.key}") String apiKey,
            @Value("${whatsapp.destinatario}") String numeroDestinatario,
            MeterRegistry meterRegistry) {
        this.baseUrl = baseUrl;
        this.instance = instance;
        this.apiKey = apiKey;
        this.numeroDestinatario = numeroDestinatario;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.enviosComSucesso = Counter.builder("relatorio.whatsapp.envio")
                .description("Envios ao WhatsApp via Evolution API")
                .tag("outcome", "sucesso")
                .register(meterRegistry);
        this.enviosComFalha = Counter.builder("relatorio.whatsapp.envio")
                .description("Envios ao WhatsApp via Evolution API")
                .tag("outcome", "erro")
                .register(meterRegistry);
    }

    public boolean estaConfigurado() {
        return apiKey != null && !apiKey.isBlank()
                && numeroDestinatario != null && !numeroDestinatario.isBlank();
    }

    @Retry(name = "whatsapp")
    public void enviarRelatorioPdf(String caminhoArquivoPdf) {
        log.info("Iniciando envio do PDF para o WhatsApp: {}", numeroDestinatario);

        Path path = Path.of(caminhoArquivoPdf);
        byte[] fileContent;
        try {
            fileContent = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new WhatsAppException("Não foi possível ler o arquivo PDF: " + caminhoArquivoPdf, e);
        }

        String base64Pdf = Base64.getEncoder().encodeToString(fileContent);
        String nomeDoArquivo = path.getFileName().toString();

        String jsonBody = """
                {
                    "number": "%s",
                    "mediaMessage": {
                        "mediatype": "document",
                        "mimetype": "application/pdf",
                        "caption": "📊 *Relatório Semanal de Aniversariantes* gerado automaticamente! Segue o PDF em anexo.",
                        "media": "%s",
                        "fileName": "%s"
                    }
                }
                """.formatted(numeroDestinatario, base64Pdf, nomeDoArquivo);

        enviar("/message/sendMedia/" + instance, jsonBody);
    }

    @Retry(name = "whatsapp")
    public void enviarMensagemTexto(String texto) {
        log.info("Enviando alerta de texto para o WhatsApp: {}", numeroDestinatario);

        String jsonBody = """
                {
                    "number": "%s",
                    "text": "%s"
                }
                """.formatted(numeroDestinatario, escaparJson(texto));

        enviar("/message/sendText/" + instance, jsonBody);
    }

    private void enviar(String caminho, String jsonBody) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + caminho))
                .header("Content-Type", "application/json")
                .header("apikey", apiKey)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Mensagem enviada com sucesso para o WhatsApp (status {}).", response.statusCode());
                enviosComSucesso.increment();
            } else {
                enviosComFalha.increment();
                throw new WhatsAppException(
                        "Falha ao enviar para o WhatsApp. Status: %d | Retorno: %s"
                                .formatted(response.statusCode(), response.body()));
            }
        } catch (InterruptedException e) {
            enviosComFalha.increment();
            Thread.currentThread().interrupt();
            throw new WhatsAppException("Envio ao WhatsApp interrompido.", e);
        } catch (IOException e) {
            enviosComFalha.increment();
            throw new WhatsAppException("Erro de comunicação com a Evolution API.", e);
        }
    }

    private String escaparJson(String texto) {
        if (texto == null) {
            return "";
        }
        return texto
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "\\n");
    }
}
