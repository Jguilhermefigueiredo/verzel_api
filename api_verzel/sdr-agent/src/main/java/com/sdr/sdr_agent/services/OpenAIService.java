package main.java.com.sdr.sdr_agent.services;

import main.java.com.sdr.sdr_agent.openai.ChatCompletionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;


@Service
public class OpenAIService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);

    private final WebClient webClient;

    public OpenAIService(@Value("${OPENAI_API_KEY}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public ChatCompletionResponse generateText(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        try {
            Mono<ChatCompletionResponse> responseMono = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(status -> status.isError(), clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        logger.error("Erro HTTP ao chamar OpenAI: {} - {}", clientResponse.statusCode(), body);
                                        return Mono.error(new RuntimeException("Erro na API OpenAI: " + clientResponse.statusCode()));
                                    })
                    )
                    .bodyToMono(ChatCompletionResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .doOnSubscribe(sub -> logger.info("Chamando OpenAI com prompt: {}", prompt))
                    .doOnSuccess(resp -> logger.info(
                            "Resposta OpenAI recebida com {} choices",
                            resp != null && resp.getChoices() != null ? resp.getChoices().size() : 0
                    ));

            ChatCompletionResponse response = responseMono.block();

            // Validação para evitar NullPointerException
            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                logger.warn("Nenhuma resposta gerada pela OpenAI para o prompt: {}", prompt);
                return null;
            }

            return response;

        } catch (WebClientResponseException e) {
            logger.error("WebClientResponseException: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Erro na comunicação com OpenAI", e);
        } catch (Exception e) {
            logger.error("Erro inesperado ao chamar OpenAI: {}", e.getMessage());
            throw new RuntimeException("Erro inesperado ao chamar OpenAI", e);
        }
    }

    // ---------- Opcional: versão assíncrona ----------
    public Mono<ChatCompletionResponse> generateTextAsync(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(
                                        "Erro na API OpenAI: " + clientResponse.statusCode() + " - " + body)))
                )
                .bodyToMono(ChatCompletionResponse.class)
                .timeout(Duration.ofSeconds(10))
                .doOnSubscribe(sub -> logger.info("Chamando OpenAI com prompt (async): {}", prompt))
                .doOnSuccess(resp -> logger.info(
                        "Resposta OpenAI recebida com {} choices (async)",
                        resp != null && resp.getChoices() != null ? resp.getChoices().size() : 0
                ));
    }
}
