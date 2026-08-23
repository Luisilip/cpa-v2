package br.edu.cpa.cpainteligente.whatsapp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

// @Slf4j (Lombok) gera automaticamente:
//   private static final Logger log = LoggerFactory.getLogger(EvolutionClient.class);
@Slf4j
@Component
public class EvolutionClient {

    private final RestClient restClient;
    private final String instance;

    // RestClient é o novo cliente HTTP do Spring Boot 3.2+
    // Mais simples e moderno que RestTemplate
    public EvolutionClient(
            @Value("${app.evolution.base-url}") String baseUrl,
            @Value("${app.evolution.api-key}")  String apiKey,
            @Value("${app.evolution.instance}") String instance
    ) {
        this.instance = instance;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("apikey", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public void sendText(String phoneNumber, String text) {
        try {
            restClient.post()
                    .uri("/message/sendText/{instance}", instance)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "number", phoneNumber,
                            "text",   text
                    ))
                    .retrieve()
                    .toBodilessEntity();

            log.debug("Mensagem enviada para {}", phoneNumber);
        } catch (Exception e) {
            // Loga mas não lança — falha no envio não deve derrubar o fluxo
            log.error("Falha ao enviar mensagem para {}: {}", phoneNumber, e.getMessage());
        }
    }
}
