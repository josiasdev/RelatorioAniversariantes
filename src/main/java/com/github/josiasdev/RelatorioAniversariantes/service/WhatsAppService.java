package com.github.josiasdev.RelatorioAniversariantes.service;

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
import java.util.Base64;

@Slf4j
@Service
public class WhatsAppService {

    private final String apiUrl;

    private final String apiKey;

    private final String numeroDestinatario;

    public WhatsAppService(
            @Value("${whatsapp.api.url}") String apiUrl,
            @Value("${whatsapp.api.key}") String apiKey,
            @Value("${whatsapp.destinatario}") String numeroDestinatario) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.numeroDestinatario = numeroDestinatario;
    }

    public void enviarRelatorioPdf(String caminhoArquivoPdf) {
        log.info("Iniciando envio do PDF para o WhatsApp: {}", numeroDestinatario);

        try {
            Path path = Path.of(caminhoArquivoPdf);
            byte[] fileContent = Files.readAllBytes(path);
            String base64Pdf = Base64.getEncoder().encodeToString(fileContent);

            String nomeDoArquivo = path.getFileName().toString();

            String jsonBody = String.format("""
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
                    """, numeroDestinatario, base64Pdf, nomeDoArquivo);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("apikey", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("PDF enviado com sucesso para o WhatsApp!");
            } else {
                log.error("Falha ao enviar para o WhatsApp. Status: {} | Retorno: {}", response.statusCode(), response.body());
            }

        } catch (IOException | InterruptedException e) {
            log.error("Erro ao tentar processar e enviar o PDF: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}